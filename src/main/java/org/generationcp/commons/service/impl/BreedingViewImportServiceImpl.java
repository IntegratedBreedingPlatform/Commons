package org.generationcp.commons.service.impl;

import com.rits.cloning.Cloner;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.breedingview.parsing.MeansCSV;
import org.generationcp.commons.breedingview.parsing.OutlierCSV;
import org.generationcp.commons.breedingview.parsing.SummaryStatsCSV;
import org.generationcp.commons.exceptions.BreedingViewImportException;
import org.generationcp.commons.service.BreedingViewImportService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.api.ontology.AnalysisVariablesImportRequest;
import org.generationcp.middleware.api.ontology.OntologyVariableService;
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
import org.generationcp.middleware.pojos.dms.ProjectProperty;
import org.generationcp.middleware.pojos.oms.CVTerm;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.service.api.analysis.SiteAnalysisService;
import org.generationcp.middleware.service.impl.analysis.SummaryStatisticsImportRequest;
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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class BreedingViewImportServiceImpl implements BreedingViewImportService {

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

	@Resource
	private SiteAnalysisService siteAnalysisService;

	@Resource
	private OntologyVariableService ontologyVariableService;

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

			if (!traitsAndMeans.isEmpty()) {

				final String[] csvHeader = traitsAndMeans.keySet().toArray(new String[0]);

				DataSet meansDataSet = this.getMeansDataSet(studyId);

				// Check if means is existing. If yes, only append the variable
				// types to existing means
				// Else, create the means dataset with the means variable types
				if (meansDataSet != null) {
					meansDataSet =
						this.appendVariableTypesToExistingMeans(csvHeader, plotDataSet, meansDataSet, study.getProgramUUID(), lsMean);
					meansDataSetExists = true;
				} else {
					meansDataSet = this.createMeansDataset(study, csvHeader, plotDataSet, lsMean);
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
					final Variable variable = new Variable(meansDataSet.getVariableTypes().findByLocalName(meansVariable), variableValue);
					list.add(variable);
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
	 * @param study       - project record of analyzed study
	 * @param csvHeader   - array of column headers from means file from BV
	 * @param plotDataSet - plot dataset of analyzed study
	 * @return means dataset created and saved
	 */
	private DataSet createMeansDataset(
		final DmsProject study, final String[] csvHeader, final DataSet plotDataSet, final CVTerm lSMean) {

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

		// Add plot dataset variables of type trial environment, ENTRY_NO and ENTRY_TYPE Entry Detail variables and germplasm to
		// means dataset (but not yet save it to the database)
		this.createMeansVariablesFromPlotDatasetAndAddToList(plotDataSet, meansVariableTypeList, 3);

		// Add analysis (mean) variable based from the import file to the means
		// dataset (but not yet save it to the database)
		this.createMeansVariablesFromImportFileAndAddToList(csvHeader, plotDataSet.getVariableTypes().getVariates(), meansVariableTypeList,
			programUUID, lSMean);

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
	 */
	void createMeansVariablesFromImportFileAndAddToList(
		final String[] csvHeader, final VariableTypeList plotVariates,
		final VariableTypeList meansVariableTypeList, final String programUUID, final CVTerm lsMean) {
		final boolean isSummaryVariable = false;
		final int numberOfMeansVariables = meansVariableTypeList.getVariableTypes().size();
		int rank = meansVariableTypeList.getVariableTypes().get(numberOfMeansVariables - 1).getRank() + 1;
		final Set<String> inputDataSetVariateNames = this.getAllNewVariatesToProcess(csvHeader, null);
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
				|| factorFromDataSet.getStandardVariable().getPhenotypicType() == PhenotypicType.GERMPLASM
				|| factorFromDataSet.getStandardVariable().getId() == TermId.ENTRY_NO.getId()
				|| factorFromDataSet.getStandardVariable().getId() == TermId.ENTRY_TYPE.getId()
				|| factorFromDataSet.getStandardVariable().getId() == TermId.OBS_UNIT_ID.getId()) {
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

	@Override
	public void importSummaryStatisticsData(final File file, final int studyId) throws BreedingViewImportException {
		try {
			// Get the summary statistics from the csv output file generated by
			// Breeding View
			final SummaryStatsCSV summaryStatsCSV = new SummaryStatsCSV(file, this.localNameToAliasMap);

			final Map<String, String> aliasToVariableNameMap =
				this.daoFactory.getProjectPropertyDAO().getByProjectId(
						this.getPlotDataSet(studyId).getId()).stream().filter(projectProperty ->
						projectProperty.getTypeId() != null && projectProperty.getVariableId() != null)
					.collect(Collectors.toMap(ProjectProperty::getAlias, pp -> pp.getVariable().getName()));

			final List<Integer> traitVariableIds =
				this.daoFactory.getCvTermDao()
					.getByNamesAndCvId(summaryStatsCSV.getTraits().stream().map(aliasToVariableNameMap::get).collect(
						Collectors.toSet()), CvId.VARIABLES, true).stream()
					.map(CVTerm::getCvTermId).collect(
						Collectors.toList());

			final AnalysisVariablesImportRequest analysisVariablesImportRequest = new AnalysisVariablesImportRequest();
			analysisVariablesImportRequest.setAnalysisMethodNames(summaryStatsCSV.getSummaryHeaders());
			analysisVariablesImportRequest.setVariableIds(traitVariableIds);
			analysisVariablesImportRequest.setVariableType(VariableType.ANALYSIS_SUMMARY.getName());

			// Create analysis summary variables
			final MultiKeyMap analysisSummaryVariablesMap =
				this.ontologyVariableService.createAnalysisVariables(analysisVariablesImportRequest,
					MapUtils.invertMap(aliasToVariableNameMap));

			final Map<String, String> environmentTrialInstanceMap =
				this.createEnvironmentTrialInstanceMap(summaryStatsCSV.getData().keySet(), studyId, summaryStatsCSV.getTrialHeader());

			final SummaryStatisticsImportRequest summaryStatisticsImportRequest =
				this.createSummaryStatisticsImportRequest(summaryStatsCSV, environmentTrialInstanceMap, analysisSummaryVariablesMap);

			// Get the summary statistics dataset if it exists.
			final List<DmsProject> datasets = this.daoFactory.getDmsProjectDAO()
				.getDatasetsByTypeForStudy(Arrays.asList(studyId), DatasetTypeEnum.SUMMARY_STATISTICS_DATA.getId());
			final CropType cropType = this.contextUtil.getProjectInContext().getCropType();
			if (CollectionUtils.isEmpty(datasets)) {
				// If the summary statistics dataset does not exist, create a new dataset
				this.siteAnalysisService.createSummaryStatisticsDataset(cropType.getCropName(), studyId, summaryStatisticsImportRequest);
			} else {
				// If it already exists, update the summary statistics dataset
				this.siteAnalysisService.updateSummaryStatisticsDataset(cropType.getCropName(), datasets.get(0).getProjectId(),
					summaryStatisticsImportRequest);
			}

		} catch (final Exception e) {
			throw new BreedingViewImportException(e.getMessage(), e);
		}

	}

	protected SummaryStatisticsImportRequest createSummaryStatisticsImportRequest(final SummaryStatsCSV summaryStatsCSV,
		final Map<String, String> environmentTrialInstanceMap, final MultiKeyMap variablesMap) throws IOException {

		final List<Integer> variableIds = new ArrayList<>(variablesMap.values());
		final Map<Integer, String> analysisSummaryVariableNamesMap =
			this.daoFactory.getCvTermDao().getByIds(variableIds).stream()
				.collect(Collectors.toMap(CVTerm::getCvTermId, CVTerm::getName));

		final SummaryStatisticsImportRequest summaryStatisticsImportRequest = new SummaryStatisticsImportRequest();
		final List<SummaryStatisticsImportRequest.SummaryData> data = new ArrayList<>();
		for (final Entry<String, String> mapEntry : environmentTrialInstanceMap.entrySet()) {
			final SummaryStatisticsImportRequest.SummaryData summaryData = new SummaryStatisticsImportRequest.SummaryData();
			summaryData.setEnvironmentNumber(Integer.valueOf(mapEntry.getValue()));
			final Map<String, Double> values = new HashMap<>();
			for (final String analysisSummaryMethodName : summaryStatsCSV.getSummaryHeaders()) {
				for (final Entry<String, List<String>> traitSummaryStat : summaryStatsCSV.getData().get(mapEntry.getKey()).entrySet()) {
					final String variableName =
						analysisSummaryVariableNamesMap.get(variablesMap.get(traitSummaryStat.getKey(), analysisSummaryMethodName));
					final String summaryStatValue =
						traitSummaryStat.getValue().get(summaryStatsCSV.getSummaryHeaders().indexOf(analysisSummaryMethodName));
					if (variableName != null) {
						values.put(variableName, StringUtils.isEmpty(summaryStatValue) ? null : Double.valueOf(summaryStatValue));
					}
				}
			}
			summaryData.setValues(values);
			data.add(summaryData);
		}
		summaryStatisticsImportRequest.setData(data);
		return summaryStatisticsImportRequest;
	}

	protected Map<String, String> createEnvironmentTrialInstanceMap(
		final Set<String> environments, final int studyId,
		final String environmentFactorName) {

		final int datasetId = this.getTrialDataSet(studyId).getId();
		final Map<String, String> environmentTrialInstanceMap = new LinkedHashMap<>();
		final TrialEnvironments trialEnvironments = this.studyDataManager.getTrialEnvironmentsInDataset(datasetId);

		final boolean isSelectedEnvironmentFactorALocation = this.studyDataManager.isLocationIdVariable(studyId, environmentFactorName);
		final Map<String, String> locationIdToNameMap = this.studyDataManager.createInstanceLocationIdToNameMapFromStudy(studyId);

		// Only create map entries for environments present in SSA Output,
		// because only these have Summary Statistic values
		// that will be saved later.
		for (final String environmentName : environments) {
			// Unfortunately, Breeding View cannot handle double quotes in CSV.
			// Because of that, variables in the CSV file with comma are
			// replaced with semicolon. So we need to replace semicolon with
			// comma again
			final String sanitizedEnvironmentFactor = environmentName.replace(";", ",");
			String trialInstanceNumber = this.getTrialInstanceNumber(trialEnvironments, environmentFactorName, sanitizedEnvironmentFactor,
				isSelectedEnvironmentFactorALocation, locationIdToNameMap);
			if (trialInstanceNumber == null) {
				trialInstanceNumber = this.getTrialInstanceNumber(trialEnvironments, environmentFactorName, environmentName,
					isSelectedEnvironmentFactorALocation, locationIdToNameMap);
			}

			environmentTrialInstanceMap.put(environmentName, trialInstanceNumber);
		}

		return environmentTrialInstanceMap;
	}

	protected String getTrialInstanceNumber(
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
			return trialEnvironment.getVariables().findById(TermId.TRIAL_INSTANCE_FACTOR.getId()).getValue();
		} else {
			return null;
		}

	}

	private String getLocationIdFromMap(final Map<String, String> locationIdMap, final String value) {
		final Optional<Entry<String, String>> entryOptional =
			locationIdMap.entrySet().stream().filter(entry -> Objects.equals(entry.getValue(), value)).findAny();
		if (entryOptional.isPresent()) {
			return entryOptional.get().getKey();
		}
		return "";
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
		final String programUUID, final CVTerm lsMean) {
		final int numberOfMeansVariables = meansDataSet.getVariableTypes().getVariableTypes().size();
		int rank = meansDataSet.getVariableTypes().getVariableTypes().get(numberOfMeansVariables - 1).getRank() + 1;
		final Set<String> traitsWithoutMeanVariable =
			this.getAllNewVariatesToProcess(csvHeader, meansDataSet.getVariableTypes().getVariates().getVariableTypes());
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
			final Term existingScale = this.ontologyDataManager.findTermByName(name, CvId.SCALES);
			if (existingScale != null) {
				return existingScale.getId();
			} else {
				final Scale scale = new Scale();
				scale.setName(name);
				scale.setDefinition(name);
				scale.setDataType(DataType.NUMERIC_VARIABLE);
				this.scaleDataManager.addScale(scale);
				return scale.getId();
			}
		} else {
			return scaleId;
		}
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
		final String[] csvHeader, final List<DMSVariableType> existingMeansVariables) {
		final Set<String> newVariateNames = new LinkedHashSet<>();
		final Optional<String> firstVariable = Arrays.stream(csvHeader)
			.filter((header) -> header.contains(MeansCSV.MEANS_SUFFIX) || //
				header.contains(MeansCSV.UNIT_ERRORS_SUFFIX) //
			).findFirst();
		final int variatesStartingIndex = Arrays.asList(csvHeader).indexOf(firstVariable.get());

		// Exclude the environment, entry, gid and Desigantion factors which are first
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
			for (final DMSVariableType dmsVariableType : existingMeansVariables) {
				String variateName = dmsVariableType.getLocalName().trim();
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
		final Variable variable = new Variable();
		variable.setValue(value);
		variable.setVariableType(vtype);
		return variable;
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
