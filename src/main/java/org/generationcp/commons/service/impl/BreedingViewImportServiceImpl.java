package org.generationcp.commons.service.impl;

import com.rits.cloning.Cloner;
import org.generationcp.commons.breedingview.parsing.MeansCSV;
import org.generationcp.commons.breedingview.parsing.OutlierCSV;
import org.generationcp.commons.breedingview.parsing.SummaryStatsCSV;
import org.generationcp.commons.constant.CommonMessage;
import org.generationcp.commons.exceptions.BreedingViewImportException;
import org.generationcp.commons.service.BreedingViewImportService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.DMSVariableType;
import org.generationcp.middleware.domain.dms.DataSet;
import org.generationcp.middleware.domain.dms.DatasetReference;
import org.generationcp.middleware.domain.dms.DatasetValues;
import org.generationcp.middleware.domain.dms.ExperimentType;
import org.generationcp.middleware.domain.dms.ExperimentValues;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.Stocks;
import org.generationcp.middleware.domain.dms.TrialEnvironment;
import org.generationcp.middleware.domain.dms.TrialEnvironments;
import org.generationcp.middleware.domain.dms.Variable;
import org.generationcp.middleware.domain.dms.VariableList;
import org.generationcp.middleware.domain.dms.VariableTypeList;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.Method;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.manager.DaoFactory;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyMethodDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyScaleDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.daoElements.OntologyVariableInfo;
import org.generationcp.middleware.operation.builder.StandardVariableBuilder;
import org.generationcp.middleware.operation.transformer.etl.StandardVariableTransformer;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.generationcp.middleware.pojos.dms.PhenotypeOutlier;
import org.generationcp.middleware.pojos.oms.CVTerm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

public class BreedingViewImportServiceImpl implements BreedingViewImportService {

	static final String PVALUE_SUFFIX = "_Pvalue";
	static final String HERITABILITY_SUFFIX = "_Heritability";
	static final String CV_SUFFIX = "_CV";
	private static final String MEAN_SUFFIX = "_Mean";
	private static final String MEAN_SED_SUFFIX = "_MeanSED";
	private static final String REGEX_VALID_BREEDING_VIEW_CHARACTERS = "[^a-zA-Z0-9-_%']+";
	private static final String LS_MEAN = "LS MEAN";

	@Autowired
	private StudyDataManager studyDataManager;

	@Autowired
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Autowired
	private OntologyDataManager ontologyDataManager;

	@Autowired
	private DaoFactory daoFactory;

	@Autowired
	private Cloner cloner;

	@Autowired
	private OntologyMethodDataManager methodDataManager;

	@Autowired
	private StandardVariableTransformer standardVariableTransformer;

	@Autowired
	private OntologyScaleDataManager scaleDataManager;

	@Autowired
	private ResourceBundleMessageSource messageSource;

	@Resource
	private ContextUtil contextUtil;

	private Map<String, String> localNameToAliasMap = new HashMap<>();

	public BreedingViewImportServiceImpl() {

	}

	public BreedingViewImportServiceImpl(
		final StudyDataManager studyDataManager,
		final OntologyVariableDataManager ontologyVariableDataManager, final OntologyMethodDataManager methodDataManager,
		final DaoFactory daoFactory, final StandardVariableTransformer standardVariableTransformer) {
		this.studyDataManager = studyDataManager;
		this.ontologyVariableDataManager = ontologyVariableDataManager;
		this.methodDataManager = methodDataManager;
		this.daoFactory = daoFactory;
		this.standardVariableTransformer = standardVariableTransformer;
	}

	/**
	 * This method is called when importing means data that is parsed from an
	 * output file generated by Breeding View
	 */
	@Override
	public void importMeansData(final File file, final int studyId) throws BreedingViewImportException {

		boolean meansDataSetExists = false;
		final CVTerm lsMean =
			this.daoFactory.getCvTermDao().getByNameAndCvId(BreedingViewImportServiceImpl.LS_MEAN, CvId.METHODS.getId());

		try {

			final DmsProject study = this.studyDataManager.getProject(studyId);
			final DataSet plotDataSet = this.getPlotDataSet(studyId);
			// Get the sanitized names to plot dataset variable names as means
			// dataset will be based on this
			this.generateNameToAliasMap(plotDataSet);

			// Get the traits and means from the csv output file generated by
			// Breeding View
			final MeansCSV meansCSV = new MeansCSV(file, this.localNameToAliasMap);
			final Map<String, List<String>> traitsAndMeans = meansCSV.getData();
			final boolean hasDuplicateColumnsInFile = meansCSV.isHasDuplicateColumns();

			if (!traitsAndMeans.isEmpty()) {

				final String[] csvHeader = traitsAndMeans.keySet().toArray(new String[0]);

				DataSet meansDataSet = this.getMeansDataSet(studyId);

				// Check if means is existing. If yes, only append the variable
				// types to existing means
				// Else, create the means dataset with the means variable types
				if (meansDataSet != null) {
					meansDataSet =
						this.appendVariableTypesToExistingMeans(csvHeader, plotDataSet, meansDataSet, study.getProgramUUID(), lsMean,
							hasDuplicateColumnsInFile);
					meansDataSetExists = true;
				} else {
					meansDataSet = this.createMeansDataset(study, csvHeader, plotDataSet, lsMean, hasDuplicateColumnsInFile);
				}

				final DataSet trialDataSet = this.getTrialDataSet(studyId);
				// Create or append the experiments to the means dataset
				this.createOrAppendMeansExperiments(meansDataSet, traitsAndMeans, meansDataSetExists, plotDataSet.getId(),
					trialDataSet.getId(), studyId);

			}
		} catch (final Exception e) {
			throw new BreedingViewImportException(e.getMessage(), e);
		}
	}

	/**
	 * This method creates or appends the experiments to the means dataset based
	 * on the map of traits and means from the output file and the existing
	 * means dataset
	 *
	 * @param meansDataSet
	 * @param traitsAndMeans
	 * @param meansDataSetExists
	 * @param plotDatasetId
	 * @param trialDatasetId
	 */
	private void createOrAppendMeansExperiments(
		final DataSet meansDataSet, final Map<String, List<String>> traitsAndMeans,
		final boolean meansDataSetExists, final int plotDatasetId, final int trialDatasetId, final int studyId) {
		final List<ExperimentValues> experimentValuesList = new ArrayList<>();
		final String[] csvHeader = traitsAndMeans.keySet().toArray(new String[0]);
		final String envHeader = csvHeader[0];
		final String entryNoHeader = csvHeader[1];
		final Map<String, Integer> envNameToNdGeolocationIdMap =
			this.createEnvironmentNameToNdGeolocationIdMap(envHeader, studyId, trialDatasetId);
		final Map<String, Integer> entroNyToStockIdMap = this.getEntryNoToStockIdMap(entryNoHeader, plotDatasetId);

		// iterate all environments in the map of traits and means based on the
		// environment factor name
		final List<String> environments = traitsAndMeans.get(envHeader);
		for (int i = 0; i < environments.size(); i++) {
			// Unfortunately, Breeding View cannot handle double quotes in CSV.
			// Because of that, variables in the CSV file with comma are
			// replaced with semicolon. So we need to replace semicolon with
			// comma again
			final String envName = environments.get(i).replace(";", ",");
			final Integer ndGeolocationId = envNameToNdGeolocationIdMap.get(envName);
			final String entryNo = traitsAndMeans.get(entryNoHeader).get(i);
			final Integer stockId = entroNyToStockIdMap.get(entryNo);

			// create experiment for the given stock id and nd_geolocation id
			final ExperimentValues experimentRow = new ExperimentValues();
			experimentRow.setGermplasmId(stockId);
			experimentRow.setLocationId(ndGeolocationId);

			final List<Variable> list = new ArrayList<>();

			// Iterate through the Mean Variable names in csv file and retrieve
			// its value for current row to save to experiment
			for (int j = 2; j < csvHeader.length; j++) {
				final String meansVariable = csvHeader[j];
				if (meansDataSetExists && meansDataSet.getVariableTypes().getVariates().findByLocalName(meansVariable) == null) {
					continue;
				}

				final String variableValue = traitsAndMeans.get(meansVariable).get(i).trim();
				if (!variableValue.trim().isEmpty()) {
					final Variable var = new Variable(meansDataSet.getVariableTypes().findByLocalName(meansVariable), variableValue);
					list.add(var);
				}

			}

			final VariableList variableList1 = new VariableList();
			variableList1.setVariables(list);
			experimentRow.setVariableList(variableList1);
			experimentValuesList.add(experimentRow);
		}

		// Save the experiments for mean dataset
		this.studyDataManager
			.addOrUpdateExperiment(this.contextUtil.getProjectInContext().getCropType(), meansDataSet.getId(), ExperimentType.AVERAGE,
				experimentValuesList);
	}

	/**
	 * This method returns a map of entry no to stock id based from the plot
	 * dataset
	 *
	 * @param entryNoHeader
	 * @param plotDatasetId
	 * @return map of entry no to stock id
	 */
	private Map<String, Integer> getEntryNoToStockIdMap(final String entryNoHeader, final int plotDatasetId) {
		final Stocks stocks = this.studyDataManager.getStocksInDataset(plotDatasetId);
		return stocks.getStockMap(entryNoHeader);
	}

	/**
	 * This method returns a map of environment factor values to nd_geolocation
	 * ids based from the trial dataset id and the environment factor name
	 *
	 * @param envFactor
	 * @param trialDatasetId
	 * @return map of environment factor names to nd_geolocation ids
	 */
	protected Map<String, Integer> createEnvironmentNameToNdGeolocationIdMap(
		final String envFactor, final int studyId,
		final int trialDatasetId) {
		final Map<String, Integer> environmentNameToGeolocationIdMap = new HashMap<>();
		final TrialEnvironments trialEnvironments = this.studyDataManager.getTrialEnvironmentsInDataset(trialDatasetId);

		final boolean isSelectedEnvironmentFactorALocation = this.studyDataManager.isLocationIdVariable(studyId, envFactor);
		final Map<String, String> locationNameMap = this.studyDataManager.createInstanceLocationIdToNameMapFromStudy(studyId);

		for (final TrialEnvironment trialEnv : trialEnvironments.getTrialEnvironments()) {
			if (isSelectedEnvironmentFactorALocation) {
				final String locationId = trialEnv.getVariables().findByLocalName(envFactor).getValue();
				final String locationName = locationNameMap.get(locationId);
				environmentNameToGeolocationIdMap.put(locationName, trialEnv.getId());
			} else {
				environmentNameToGeolocationIdMap.put(trialEnv.getVariables().findByLocalName(envFactor).getValue(), trialEnv.getId());
			}

		}
		return environmentNameToGeolocationIdMap;
	}

	/**
	 * Create the mean dataset based on the map of traits and means from the
	 * output file and save it to the database
	 *
	 * @param study                     - project record of analyzed study
	 * @param csvHeader                 - array of column headers from means file from BV
	 * @param plotDataSet               - plot dataset of analyzed study
	 * @param hasDuplicateColumnsInFile - flag whether duplicate columns were found in means file from
	 *                                  BV
	 * @return means dataset created and saved
	 */
	private DataSet createMeansDataset(
		final DmsProject study, final String[] csvHeader, final DataSet plotDataSet, final CVTerm lSMean,
		final boolean hasDuplicateColumnsInFile) {

		final VariableTypeList meansVariableTypeList = new VariableTypeList();
		final VariableList meansVariableList = new VariableList();
		final DatasetValues datasetValues = new DatasetValues();
		datasetValues.setVariables(meansVariableList);

		// Add dataset type variables to means dataset (but not yet save it to
		// the database)
		final String programUUID = study.getProgramUUID();
		final String datasetName = study.getName() + "-MEANS";
		this.addMeansVariableToLists(
			this.createMeansVariable(TermId.DATASET_NAME.getId(), datasetName, "Dataset name (local)", datasetName, 1, programUUID,
				PhenotypicType.DATASET), meansVariableList, meansVariableTypeList);

		this.addMeansVariableToLists(
			this.createMeansVariable(TermId.DATASET_TITLE.getId(), "DATASET_TITLE", "Dataset title (local)", "My Dataset Description",
				2, programUUID, PhenotypicType.DATASET), meansVariableList, meansVariableTypeList);

		// Add plot dataset variables of type trial environment and germplasm to
		// means dataset (but not yet save it to the database)
		this.createMeansVariablesFromPlotDatasetAndAddToList(plotDataSet, meansVariableTypeList, 3);

		// Add analysis (mean) variable based from the import file to the means
		// dataset (but not yet save it to the database)
		this.createMeansVariablesFromImportFileAndAddToList(csvHeader, plotDataSet.getVariableTypes().getVariates(), meansVariableTypeList,
			programUUID, lSMean, hasDuplicateColumnsInFile);

		// Save and return the newly-created means dataset
		final DatasetReference datasetReference =
			this.studyDataManager
				.addDataSet(study.getProjectId(), meansVariableTypeList, datasetValues, programUUID, DatasetTypeEnum.MEANS_DATA.getId());

		return this.studyDataManager.getDataSet(datasetReference.getId());

	}

	/**
	 * Create analysis variable (means) for and add
	 *
	 * @param csvHeader
	 * @param plotVariates
	 * @param meansVariableTypeList
	 * @param programUUID
	 * @param lsMean
	 * @param hasDuplicateColumnsInFile
	 */
	void createMeansVariablesFromImportFileAndAddToList(
		final String[] csvHeader, final VariableTypeList plotVariates,
		final VariableTypeList meansVariableTypeList, final String programUUID, final CVTerm lsMean,
		final boolean hasDuplicateColumnsInFile) {
		final boolean isSummaryVariable = false;
		final int numberOfMeansVariables = meansVariableTypeList.getVariableTypes().size();
		int rank = meansVariableTypeList.getVariableTypes().get(numberOfMeansVariables - 1).getRank() + 1;
		final Set<String> inputDataSetVariateNames = this.getAllNewVariatesToProcess(csvHeader, null, hasDuplicateColumnsInFile);
		final Term lsMeanTerm = new Term(lsMean.getCvTermId(), lsMean.getName(), lsMean.getDefinition());

		for (final String variateName : inputDataSetVariateNames) {
			final DMSVariableType variate = plotVariates.findByLocalName(variateName);
			meansVariableTypeList.add(this
				.createAnalysisVariable(variate, variateName + MeansCSV.MEANS_SUFFIX, lsMeanTerm, programUUID, rank++,
					isSummaryVariable));
		}
	}

	/**
	 * Create plot dataset variables of type trial environment and germplasm and
	 * add to means dataset variable type list
	 *
	 * @param plotDataSet
	 * @param meansVariableTypeList
	 * @param lastRank
	 */
	private void createMeansVariablesFromPlotDatasetAndAddToList(
		final DataSet plotDataSet, final VariableTypeList meansVariableTypeList,
		final int lastRank) {
		int rank = lastRank;
		for (final DMSVariableType factorFromDataSet : plotDataSet.getVariableTypes().getFactors().getVariableTypes()) {
			if (factorFromDataSet.getStandardVariable().getPhenotypicType() == PhenotypicType.TRIAL_ENVIRONMENT
				|| factorFromDataSet.getStandardVariable().getPhenotypicType() == PhenotypicType.GERMPLASM) {
				factorFromDataSet.setRank(++rank);
				meansVariableTypeList.add(factorFromDataSet);
			}
		}
	}

	/**
	 * Add variable to the means variable list and means variable type list
	 *
	 * @param variable
	 * @param meansVariableList
	 * @param meansVariableTypeList
	 */
	private void addMeansVariableToLists(
		final Variable variable, final VariableList meansVariableList,
		final VariableTypeList meansVariableTypeList) {
		meansVariableList.add(variable);
		meansVariableTypeList.add(variable.getVariableType());
	}

	/**
	 * This method creates the means variable with the correct variable type
	 *
	 * @param ontologyVariableId
	 * @param name
	 * @param definition
	 * @param value
	 * @param rank
	 * @param programUUID
	 * @param phenotypicType
	 * @return means variable in the means dataset
	 */
	private Variable createMeansVariable(
		final int ontologyVariableId, final String name, final String definition, final String value,
		final int rank, final String programUUID, final PhenotypicType phenotypicType) {
		final Variable variable = this.createVariable(ontologyVariableId, value, rank, programUUID, phenotypicType);
		final VariableType variableType = new StandardVariableBuilder(null).mapPhenotypicTypeToDefaultVariableType(phenotypicType, true);
		this.updateDMSVariableType(variable.getVariableType(), name, definition, variableType);
		return variable;
	}

	/**
	 * This method returns the means dataset of the study from the database
	 *
	 * @param studyId
	 * @return means dataset
	 */
	private DataSet getMeansDataSet(final int studyId) {
		return this.studyDataManager.findOneDataSetByType(studyId, DatasetTypeEnum.MEANS_DATA.getId());
	}

	/**
	 * This method returns the map of method name to method id from the list of
	 * method names in the database. If the method name does not exist, it is
	 * created and returned from the database.
	 *
	 * @param methodNameList
	 * @return
	 */
	private Map<String, Integer> findOrSaveMethodsIfNotExisting(final List<String> methodNameList) {
		final Map<String, Integer> methodNameToIdMap = new HashMap<>();
		for (final String methodName : methodNameList) {
			final Integer methodId = this.findOrSaveMethod(methodName, methodName + "  (system generated method)");
			methodNameToIdMap.put(methodName, methodId);
		}
		return methodNameToIdMap;
	}

	/**
	 * This method finds or save the method from the database based from the
	 * method name
	 *
	 * @param methodName
	 * @param methodDefinition
	 * @return method id
	 */
	private Integer findOrSaveMethod(final String methodName, final String methodDefinition) {
		Integer methodId = null;
		final CVTerm cvterm = this.daoFactory.getCvTermDao().getByNameAndCvId(methodName, CvId.METHODS.getId());
		if (cvterm == null) {
			methodId = this.saveMethod(methodName, methodDefinition);
		} else {
			methodId = cvterm.getCvTermId();
		}
		return methodId;
	}

	/**
	 * This creates a Method instance, save it and return the newly-created
	 * method id
	 *
	 * @param methodName
	 * @param methodDefinition
	 * @return method id
	 */
	private Integer saveMethod(final String methodName, final String methodDefinition) {
		final Method method = new Method();
		method.setName(methodName);
		method.setDefinition(methodDefinition);
		this.methodDataManager.addMethod(method);
		return method.getId();
	}

	/**
	 * This method checks if the ontology variable with given name exists
	 *
	 * @param variableName
	 * @return boolean - true if variable exists, else false
	 */
	private boolean isVariableExisting(final String variableName) {
		final CVTerm cvterm = this.daoFactory.getCvTermDao().getByNameAndCvId(variableName, CvId.VARIABLES.getId());
		return cvterm != null;
	}

	/**
	 * This method saves an ontology variable with variable type Analysis and
	 * returns the newly-created id
	 *
	 * @param name
	 * @param description
	 * @param methodId
	 * @param propertyId
	 * @param scaleId
	 * @param programUUID
	 * @return ontology variable id
	 */
	private Integer saveAnalysisVariable(
		final String name, final String description, final int methodId, final int propertyId,
		final int scaleId, final String programUUID, final boolean isSummaryVariable) {
		final OntologyVariableInfo variableInfo = new OntologyVariableInfo();
		variableInfo.setName(name);
		variableInfo.setDescription(description);
		variableInfo.setMethodId(methodId);
		variableInfo.setPropertyId(propertyId);
		variableInfo.setScaleId(scaleId);
		variableInfo.setProgramUuid(programUUID);
		variableInfo.addVariableType(isSummaryVariable ? VariableType.ANALYSIS_SUMMARY : VariableType.ANALYSIS);
		this.ontologyVariableDataManager.addVariable(variableInfo);
		return variableInfo.getId();
	}

	/**
	 * This method is used to import summary statistics generated by Breeding
	 * View to trial dataset
	 */
	@Override
	public void importSummaryStatsData(final File file, final int studyId) throws BreedingViewImportException {

		try {
			final DataSet plotDataset = this.getPlotDataSet(studyId);
			// Get the sanitized names to plot dataset variable names as means
			// dataset will be based on this
			this.generateNameToAliasMap(plotDataset);

			// Get the summary statistics from the csv output file generated by
			// Breeding View
			final SummaryStatsCSV summaryStatsCSV = new SummaryStatsCSV(file, this.localNameToAliasMap);

			final DmsProject study = this.studyDataManager.getProject(studyId);
			final String programUUID = study.getProgramUUID();
			final DataSet trialDataSet = this.getTrialDataSet(studyId);

			// Used in getting the new project properties
			final VariableTypeList variableTypeListVariates = plotDataset.getVariableTypes().getVariates();

			// Create the summary statistics variable types
			final VariableTypeList summaryStatsVariableTypeList =
				this.createSummaryStatsVariableTypes(summaryStatsCSV, trialDataSet, variableTypeListVariates, programUUID);

			final Map<Integer, String> geolocationIdToEnvironmentMap =
				this.createGeolocationIdEnvironmentMap(summaryStatsCSV.getData().keySet(), studyId, summaryStatsCSV.getTrialHeader());
			final List<ExperimentValues> summaryStatsExperimentValuesList =
				this.createSummaryStatsExperimentValuesList(trialDataSet, geolocationIdToEnvironmentMap, summaryStatsCSV);

			// Save project properties and experiments
			final DmsProject project = new DmsProject();
			project.setProjectId(trialDataSet.getId());
			project.setStudyType(study.getStudyType());

			this.studyDataManager.saveTrialDatasetSummary(project, summaryStatsVariableTypeList, summaryStatsExperimentValuesList,
				new ArrayList<>(geolocationIdToEnvironmentMap.keySet()));

		} catch (final Exception e) {
			throw new BreedingViewImportException(e.getMessage(), e);
		}

	}

	/**
	 * Create list ExperimentValues for each summary statistic method, for each
	 * trait and environment analyzed with proper value coming from parsed
	 * summary file from Breeding View
	 *
	 * @param trialDataSet                  - environment dataset of analyzed study
	 * @param geolocationIdToEnvironmentMap - map of Trial Environment ID (nd_geolocation ID) to
	 *                                      environment names
	 * @param summaryCSV                    - summary headers and data from summary file from Breeding
	 *                                      View
	 * @return list of ExperimentValues generated from summary data
	 * @throws IOException
	 */
	List<ExperimentValues> createSummaryStatsExperimentValuesList(
		final DataSet trialDataSet,
		final Map<Integer, String> geolocationIdToEnvironmentMap, final SummaryStatsCSV summaryCSV) throws IOException {
		final List<String> summaryHeaders = summaryCSV.getSummaryHeaders();
		final Map<String, Map<String, List<String>>> summaryStatsData = summaryCSV.getData();
		final List<ExperimentValues> summaryStatsExperimentValuesList = new ArrayList<>();

		int counter = 0;
		for (final String envFactorValue : geolocationIdToEnvironmentMap.values()) {
			for (final String summaryStatName : summaryHeaders) {
				for (final Entry<String, List<String>> traitSummaryStat : summaryStatsData.get(envFactorValue).entrySet()) {

					final VariableList variableList = new VariableList();
					variableList.setVariables(new ArrayList<Variable>());
					final ExperimentValues experimentValues = new ExperimentValues();
					experimentValues.setVariableList(variableList);
					experimentValues.setLocationId(Integer.valueOf(geolocationIdToEnvironmentMap.keySet().toArray()[counter].toString()));

					final DMSVariableType summaryStatVariableType =
						trialDataSet.findVariableTypeByLocalName(traitSummaryStat.getKey() + "_" + summaryStatName);

					if (summaryStatVariableType != null) {
						final String summaryStatValue = traitSummaryStat.getValue().get(summaryHeaders.indexOf(summaryStatName));
						final Variable var = new Variable(summaryStatVariableType, summaryStatValue);
						experimentValues.getVariableList().getVariables().add(var);
						summaryStatsExperimentValuesList.add(experimentValues);
					}
				}
			}
			counter++;
		}
		return summaryStatsExperimentValuesList;
	}

	protected Map<Integer, String> createGeolocationIdEnvironmentMap(
		final Set<String> environments, final int studyId,
		final String environmentFactorName) {

		final int datasetId = this.getTrialDataSet(studyId).getId();
		final Map<Integer, String> envFactorTolocationIdMap = new LinkedHashMap<>();
		final TrialEnvironments trialEnvironments = this.studyDataManager.getTrialEnvironmentsInDataset(datasetId);

		final boolean isSelectedEnvironmentFactorALocation = this.studyDataManager.isLocationIdVariable(studyId, environmentFactorName);
		final Map<String, String> locationNameToIdMap = this.studyDataManager.createInstanceLocationIdToNameMapFromStudy(studyId);

		// Only create map entries for environments present in SSA Output,
		// because only these have Summary Statistic values
		// that will be saved later.
		for (final String environmentName : environments) {
			// Unfortunately, Breeding View cannot handle double quotes in CSV.
			// Because of that, variables in the CSV file with comma are
			// replaced with semicolon. So we need to replace semicolon with
			// comma again
			final String sanitizedEnvironmentFactor = environmentName.replace(";", ",");
			Integer geolocationId = this.getTrialEnvironmentId(trialEnvironments, environmentFactorName, sanitizedEnvironmentFactor,
				isSelectedEnvironmentFactorALocation, locationNameToIdMap);
			if (geolocationId == null) {
				geolocationId = this.getTrialEnvironmentId(trialEnvironments, environmentFactorName, environmentName,
					isSelectedEnvironmentFactorALocation, locationNameToIdMap);
			}

			envFactorTolocationIdMap.put(geolocationId, environmentName);
		}

		return envFactorTolocationIdMap;
	}

	protected Integer getTrialEnvironmentId(
		final TrialEnvironments trialEnvironments, final String environmentFactor,
		final String environmentName, final boolean isSelectedEnvironmentFactorALocation,
		final Map<String, String> locationNameToIdMap) {

		TrialEnvironment trialEnvironment = null;

		if (isSelectedEnvironmentFactorALocation) {
			final String locationId = this.getLocationIdFromMap(locationNameToIdMap, environmentName);
			trialEnvironment = trialEnvironments.findOnlyOneByLocalName(environmentFactor, locationId);
		} else {
			trialEnvironment = trialEnvironments.findOnlyOneByLocalName(environmentFactor, environmentName);
		}

		if (trialEnvironment != null) {
			return trialEnvironment.getId();
		} else {
			return null;
		}

	}

	private String getLocationIdFromMap(final Map<String, String> locationIdMap, final String value){
		return locationIdMap.entrySet().stream().filter(entry -> Objects.equals(entry.getValue(), value)).findAny().get().getKey();
	}

	/**
	 * This method creates the summary statistics variable types from the
	 * relevant summary statistics columns for traits included in the summary
	 * output file from BV.
	 *
	 * @param summaryStatsCSV
	 * @param trialDataSet    - environment dataset of study analyzed
	 * @param plotVariates    - traits of study analyzed
	 * @param programUUID     - unique UUID of program where the study belongs to
	 * @return VariableTypeList containing the summary statistics variable types
	 * @throws IOException
	 */
	VariableTypeList createSummaryStatsVariableTypes(
		final SummaryStatsCSV summaryStatsCSV, final DataSet trialDataSet,
		final VariableTypeList plotVariates, final String programUUID) throws IOException {
		final VariableTypeList summaryStatsVariableTypeList = new VariableTypeList();

		final List<String> summaryHeaders = summaryStatsCSV.getSummaryHeaders();
		final Map<String, Integer> summaryStatNameToIdMap = this.findOrSaveMethodsIfNotExisting(summaryHeaders);

		final boolean isSummaryVariable = true;
		final int numberOfTrialDatasetVariables = trialDataSet.getVariableTypes().size();
		int rank = trialDataSet.getVariableTypes().getVariableTypes().get(numberOfTrialDatasetVariables - 1).getRank() + 1;

		// Iterate through relevant summary statistics gathered per trait the
		// output file of Breeding View
		for (final String summaryStatName : summaryHeaders) {
			for (final String trait : summaryStatsCSV.getTraits()) {
				final String localName = trait + "_" + summaryStatName;

				// Only create summary statistic variable if not yet existing in
				// trial dataset
				if (this.localNameToAliasMap.containsValue(trait) && trialDataSet.findVariableTypeByLocalName(localName) == null) {
					final DMSVariableType originalVariableType = plotVariates.findByLocalName(trait);
					// Use the method generated for the summary statistic
					// variable
					final Term summaryStatMethod = new Term(summaryStatNameToIdMap.get(summaryStatName), summaryStatName, summaryStatName);
					// Create the summary statistic variable type and add to
					// list
					final DMSVariableType summaryStatVariableType =
						this.createAnalysisVariable(originalVariableType, localName, summaryStatMethod, programUUID, rank++,
							isSummaryVariable);
					summaryStatVariableType.setVariableType(VariableType.ANALYSIS_SUMMARY);
					summaryStatsVariableTypeList.add(summaryStatVariableType);
					trialDataSet.getVariableTypes().add(summaryStatVariableType);

				}
			}
		}

		return summaryStatsVariableTypeList;
	}

	/**
	 * This method imports the outliers from the outlier output file generated
	 * by Breeding View
	 */
	@Override
	public void importOutlierData(final File file, final int studyId) throws BreedingViewImportException {

		try {
			final DataSet plotDataset = this.getPlotDataSet(studyId);
			this.generateNameToAliasMap(plotDataset);

			final OutlierCSV outlierCSV = new OutlierCSV(file, this.localNameToAliasMap);
			final Map<String, Map<String, List<String>>> outlierData = outlierCSV.getData();
			final Map<String, Integer> ndGeolocationIds = new HashMap<>();

			final Map<Integer, Integer> stdVariableIds = new HashMap<>();
			final VariableTypeList plotVariableTypeList = plotDataset.getVariableTypes();

			Integer i = 0;
			for (final String l : outlierCSV.getHeaderTraits()) {
				final Integer traitId = plotVariableTypeList.findByLocalName(l).getId();
				stdVariableIds.put(i, traitId);
				i++;
			}

			final TrialEnvironments trialEnvironments = this.studyDataManager.getTrialEnvironmentsInDataset(plotDataset.getId());
			for (final TrialEnvironment trialEnv : trialEnvironments.getTrialEnvironments()) {
				ndGeolocationIds.put(trialEnv.getVariables().findByLocalName(outlierCSV.getTrialHeader()).getValue(), trialEnv.getId());
			}

			// iterate all environments from the outlier data
			final Set<String> environments = outlierData.keySet();
			for (final String env : environments) {

				final List<PhenotypeOutlier> outliers = new ArrayList<>();
				final Integer ndGeolocationId = ndGeolocationIds.get(env);

				// iterate all variables with outliers
				for (final Entry<String, List<String>> plot : outlierData.get(env).entrySet()) {

					final List<Integer> cvTermIds = new ArrayList<>();
					final Integer plotNo = Integer.valueOf(plot.getKey());
					final Map<Integer, String> plotMap = new HashMap<>();

					for (int x = 0; x < plot.getValue().size(); x++) {
						final String traitValue = plot.getValue().get(x);
						if (traitValue.isEmpty()) {
							cvTermIds.add(stdVariableIds.get(x));
							plotMap.put(stdVariableIds.get(x), traitValue);
						}

					}

					// retrieve all phenotype id of variables based on the plot
					// no
					final List<Object[]> list = this.studyDataManager
						.getPhenotypeIdsByLocationAndPlotNo(plotDataset.getId(), ndGeolocationId, plotNo, cvTermIds);
					for (final Object[] object : list) {
						// create PhenotypeOutlier objects and add to list
						final PhenotypeOutlier outlier = new PhenotypeOutlier();
						outlier.setPhenotypeId(Integer.valueOf(object[2].toString()));
						outlier.setValue(plotMap.get(Integer.valueOf(object[1].toString())));
						outliers.add(outlier);
					}

				}

				// save the outliers in the database
				this.studyDataManager.saveOrUpdatePhenotypeOutliers(outliers);
			}
		} catch (final Exception e) {
			throw new BreedingViewImportException(e.getMessage(), e);
		}

	}

	/**
	 * This method returns the plot dataset of the study from the database
	 *
	 * @param studyId
	 * @return plot dataset
	 */
	protected DataSet getPlotDataSet(final int studyId) {
		return this.studyDataManager.findOneDataSetByType(studyId, DatasetTypeEnum.PLOT_DATA.getId());
	}

	/**
	 * This method returns the trial dataset of the study from the database
	 *
	 * @param studyId
	 * @return trial dataset
	 */
	protected DataSet getTrialDataSet(final int studyId) {
		return this.studyDataManager.findOneDataSetByType(studyId, DatasetTypeEnum.SUMMARY_DATA.getId());
	}

	/**
	 * Add the mean variables to the existing means and save it to the database
	 *
	 * @param csvHeader                 - array of headers in means file generated by BV
	 * @param plotDataSet               - plot dataset of analyzed study
	 * @param meansDataSet              - mean dataset of analyzed study
	 * @param programUUID               - unique UUID of program to which analyzed study belongs
	 * @param lsMean                    - cvterm of mean method
	 * @param hasDuplicateColumnsInFile - flag whether summary file from BV had duplicate columns
	 * @return means dataset
	 */
	// FIXME No need to return back input parameter meansDataSet
	protected DataSet appendVariableTypesToExistingMeans(
		final String[] csvHeader, final DataSet plotDataSet, final DataSet meansDataSet,
		final String programUUID, final CVTerm lsMean, final boolean hasDuplicateColumnsInFile) {
		final int numberOfMeansVariables = meansDataSet.getVariableTypes().getVariableTypes().size();
		int rank = meansDataSet.getVariableTypes().getVariableTypes().get(numberOfMeansVariables - 1).getRank() + 1;
		final Set<String> traitsWithoutMeanVariable =
			this.getAllNewVariatesToProcess(csvHeader, meansDataSet.getVariableTypes().getVariates().getVariableTypes(),
				hasDuplicateColumnsInFile);
		final Term lsMeanTerm = new Term(lsMean.getCvTermId(), lsMean.getName(), lsMean.getDefinition());
		final boolean isSummaryVariable = false;
		for (final String variateName : traitsWithoutMeanVariable) {
			final DMSVariableType variate = plotDataSet.getVariableTypes().findByLocalName(variateName);
			// add means of the variate to the means dataset
			this.addVariableToDataset(
				meansDataSet,
				this.createAnalysisVariable(variate, variateName + MeansCSV.MEANS_SUFFIX, lsMeanTerm, programUUID, rank++,
					isSummaryVariable));
		}

		return meansDataSet;
	}

	/**
	 * This saves the variable type under the dataset in the database
	 *
	 * @param dataSet
	 * @param meansVariableType
	 */
	private void addVariableToDataset(final DataSet dataSet, final DMSVariableType meansVariableType) {
		this.studyDataManager.addDataSetVariableType(dataSet.getId(), meansVariableType);
		dataSet.getVariableTypes().add(meansVariableType);
	}

	/***
	 * This method creates the analysis variable based from the variates in the
	 * plot dataset. Basically, the difference between the original variate and
	 * the new means variable is their name and the ontology variable where they
	 * are associated, having a different method and having no specific variable
	 * value constraints. This method also creates the ontology variable if it
	 * is still not existing.
	 *
	 * @param originalVariableType
	 *            - the variate where the analysis variable will be based
	 * @param name
	 *            - the name of the analysis variable
	 * @param method
	 *            - the method of the analysis variable
	 * @param programUUID
	 *            - the program where the analysis belongs
	 * @param rank
	 *            - the rank of the analysis variable from the list
	 * @return DMSVariableType - the new analysis variable
	 */
	protected DMSVariableType createAnalysisVariable(
		final DMSVariableType originalVariableType, final String name, final Term method,
		final String programUUID, final int rank, final boolean isSummaryVariable) {
		final DMSVariableType analysisVariableType = this.cloner.deepClone(originalVariableType);
		analysisVariableType.setLocalName(name);
		final StandardVariable standardVariable = analysisVariableType.getStandardVariable();
		standardVariable.setMethod(method);

		Integer analysisVariableID = this.ontologyDataManager
			.retrieveDerivedAnalysisVariable(originalVariableType.getStandardVariable().getId(), method.getId());
		if (analysisVariableID == null) {
			String variableName = name;
			if (this.isVariableExisting(variableName)) {
				variableName = variableName + "_1";
			}

			final int scaleId = this.getAnalysisVariableScaleId(standardVariable.getScale().getId(), name);
			analysisVariableID =
				this.saveAnalysisVariable(variableName, standardVariable.getDescription(), standardVariable.getMethod().getId(),
					standardVariable.getProperty().getId(), scaleId, programUUID, isSummaryVariable);
			this.ontologyDataManager.addCvTermRelationship(originalVariableType.getStandardVariable().getId(), analysisVariableID,
				TermId.HAS_ANALYSIS_VARIABLE.getId());

			standardVariable.setId(analysisVariableID);
			standardVariable.setPhenotypicType(PhenotypicType.VARIATE);

		} else {
			analysisVariableType
				.setStandardVariable(this.createStandardardVariable(analysisVariableID, programUUID, PhenotypicType.VARIATE));
		}

		analysisVariableType.setRank(rank);
		return analysisVariableType;
	}

	int getAnalysisVariableScaleId(final int scaleId, final String name) {
		final Scale originalScale = this.scaleDataManager.getScaleById(scaleId, true);
		// Create new scales for analysis variables if the original scale is
		// categorical else retain the original scale
		if (originalScale.getDataType().getId() == TermId.CATEGORICAL_VARIABLE.getId()) {
			final String scaleName = this.generateAnalysisVariableScaleName(name);
			final Term existingScale = this.ontologyDataManager.findTermByName(scaleName, CvId.SCALES);
			if (existingScale != null) {
				return existingScale.getId();
			} else {
				final Scale scale = new Scale();
				scale.setName(scaleName);
				scale.setDefinition(scaleName);
				scale.setDataType(DataType.NUMERIC_VARIABLE);
				this.scaleDataManager.addScale(scale);
				return scale.getId();
			}
		} else {
			return scaleId;
		}
	}

	String generateAnalysisVariableScaleName(final String name) {
		final String variableName = name.substring(0, name.lastIndexOf('_'));
		String scaleName = "";
		if (name.endsWith(MeansCSV.MEANS_SUFFIX) || name.endsWith(BreedingViewImportServiceImpl.MEAN_SED_SUFFIX) || name
			.endsWith(BreedingViewImportServiceImpl.MEAN_SUFFIX)) {
			scaleName = this.messageSource.getMessage(CommonMessage.MEANS_SCALE_NAME.name(), new Object[] {variableName}, Locale.ENGLISH);
		} else if (name.endsWith(BreedingViewImportServiceImpl.CV_SUFFIX)) {
			scaleName = this.messageSource.getMessage(CommonMessage.CV_SCALE_NAME.name(), new Object[] {variableName}, Locale.ENGLISH);
		} else if (name.endsWith(BreedingViewImportServiceImpl.HERITABILITY_SUFFIX)) {
			scaleName = this.messageSource
				.getMessage(CommonMessage.HERITABILITY_SCALE_NAME.name(), new Object[] {variableName}, Locale.ENGLISH);
		} else if (name.endsWith(BreedingViewImportServiceImpl.PVALUE_SUFFIX)) {
			scaleName = this.messageSource.getMessage(CommonMessage.PVALUE_SCALE_NAME.name(), new Object[] {variableName}, Locale.ENGLISH);
		}
		return scaleName;
	}

	/***
	 * This method processes the headers from the CSV file which are list of
	 * means variable names. The variate names are extracted from the headers
	 * and added to the list. Variates with existing means variables are removed
	 * from the list to be returned.
	 *
	 * @param csvHeader
	 *            - the array of headers from the CSV file
	 * @param existingMeansVariables
	 *            - existing means variables in the means dataset of the study
	 * @return Set<String> - unique list of new variates
	 */
	private Set<String> getAllNewVariatesToProcess(
		final String[] csvHeader, final List<DMSVariableType> existingMeansVariables,
		final boolean hasDuplicateColumnsInFile) {
		final Set<String> newVariateNames = new LinkedHashSet<>();
		int variatesStartingIndex = 3;
		if (hasDuplicateColumnsInFile) {
			variatesStartingIndex = 2;
		}
		// Exclude the environment, entry # and gid factors which are first
		// column headers
		final List<String> inputDataSetVariateNames =
			new ArrayList<>(Arrays.asList(Arrays.copyOfRange(csvHeader, variatesStartingIndex, csvHeader.length)));

		for (final String csvHeaderNames : inputDataSetVariateNames) {
			final String variateName = csvHeaderNames.substring(0, csvHeaderNames.lastIndexOf('_'));
			newVariateNames.add(variateName);
		}

		// Only process the new traits that were not part of the previous
		// analysis
		if (existingMeansVariables != null) {
			for (final DMSVariableType var : existingMeansVariables) {
				String variateName = var.getLocalName().trim();
				variateName = variateName.substring(0, variateName.lastIndexOf('_'));
				newVariateNames.remove(variateName);
			}
		}

		return newVariateNames;
	}

	/**
	 * This method returns a standard variable object from the database given
	 * the ontology variable field values
	 *
	 * @param termId
	 * @param programUUID
	 * @param phenotypicType
	 * @return StandardVariable instance of the ontology variable
	 */
	protected StandardVariable createStandardardVariable(final int termId, final String programUUID, final PhenotypicType phenotypicType) {
		final org.generationcp.middleware.domain.ontology.Variable ontologyVariable =
			this.ontologyVariableDataManager.getVariable(programUUID, termId, false);
		final StandardVariable standardVariable = this.standardVariableTransformer.transformVariable(ontologyVariable);
		standardVariable.setPhenotypicType(phenotypicType);
		return standardVariable;
	}

	/**
	 * This method returns a Variable object given the ontology variable field
	 * values
	 *
	 * @param termId
	 * @param value
	 * @param rank
	 * @param programUUID
	 * @param phenotypicType
	 * @return Variable instance
	 */
	protected Variable createVariable(
		final int termId, final String value, final int rank, final String programUUID,
		final PhenotypicType phenotypicType) {

		final StandardVariable stVar = this.createStandardardVariable(termId, programUUID, phenotypicType);

		final DMSVariableType vtype = new DMSVariableType();
		vtype.setStandardVariable(stVar);
		vtype.setRank(rank);
		vtype.setRole(phenotypicType);
		final Variable var = new Variable();
		var.setValue(value);
		var.setVariableType(vtype);
		return var;
	}

	/**
	 * This method sets the name, description and variable type of the
	 * DMSVariableType object
	 *
	 * @param type
	 * @param name
	 * @param description
	 * @param variableType
	 */
	protected void updateDMSVariableType(
		final DMSVariableType type, final String name, final String description,
		final VariableType variableType) {
		type.setLocalName(name);
		type.setLocalDescription(description);
		type.setVariableType(variableType);
	}

	/**
	 * If there's existing alias to local name map, return it. Otherwise,
	 * retrieve the existing plot dataset variables and create a map of
	 * sanitized names to the variable names
	 *
	 * @param plotDataset - plot dataset of analyzed study
	 * @return Map of sanitized names to local variable names
	 */
	protected void generateNameToAliasMap(final DataSet plotDataset) {

		if (this.localNameToAliasMap.isEmpty()) {
			final List<DMSVariableType> variateList = plotDataset.getVariableTypes().getVariableTypes();

			this.localNameToAliasMap = new HashMap<>();

			String entryNoName = null;
			for (final Iterator<DMSVariableType> variateListIterator = variateList.iterator(); variateListIterator.hasNext(); ) {
				final DMSVariableType variable = variateListIterator.next();
				if (variable.getStandardVariable().getId() == TermId.ENTRY_NO.getId()) {
					entryNoName = variable.getLocalName();
				}
				final String nameSanitized =
					variable.getLocalName().replaceAll(BreedingViewImportServiceImpl.REGEX_VALID_BREEDING_VIEW_CHARACTERS, "_");
				this.localNameToAliasMap.put(nameSanitized, variable.getLocalName());
			}

			this.mapDupeEntryNoToActualEntryNo(this.localNameToAliasMap, entryNoName);
		}
	}

	// This will handle the duplicate entry no generated by Breeding View if
	// ENTRY_NO is used a genotypes value
	private void mapDupeEntryNoToActualEntryNo(final Map<String, String> nameAliasMap, final String entryNoName) {
		nameAliasMap.put(entryNoName + "_1", entryNoName);
	}

	protected void setCloner(final Cloner cloner) {
		this.cloner = cloner;
	}

	protected void setMessageSource(final ResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public void setLocalNameToAliasMap(final Map<String, String> localNameToAliasMap) {
		this.localNameToAliasMap = localNameToAliasMap;
	}

	public Map<String, String> getLocalNameToAliasMap() {
		return this.localNameToAliasMap;
	}

}
