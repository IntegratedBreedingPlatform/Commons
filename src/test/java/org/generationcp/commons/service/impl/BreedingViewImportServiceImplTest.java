package org.generationcp.commons.service.impl;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.rits.cloning.Cloner;
import org.generationcp.commons.breedingview.parsing.MeansCSV;
import org.generationcp.commons.breedingview.parsing.SummaryStatsCSV;
import org.generationcp.commons.data.initializer.SummaryStatsTestDataInitializer;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.dao.oms.CVTermDao;
import org.generationcp.middleware.data.initializer.OntologyScaleTestDataInitializer;
import org.generationcp.middleware.domain.dms.DMSVariableType;
import org.generationcp.middleware.domain.dms.DataSet;
import org.generationcp.middleware.domain.dms.DatasetReference;
import org.generationcp.middleware.domain.dms.DatasetValues;
import org.generationcp.middleware.domain.dms.ExperimentType;
import org.generationcp.middleware.domain.dms.ExperimentValues;
import org.generationcp.middleware.domain.dms.LocationDto;
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
import org.generationcp.middleware.manager.ontology.daoElements.VariableFilter;
import org.generationcp.middleware.operation.transformer.etl.StandardVariableTransformer;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.generationcp.middleware.pojos.dms.PhenotypeOutlier;
import org.generationcp.middleware.pojos.oms.CVTerm;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BreedingViewImportServiceImplTest {

	private static final String ACDTOL_E_1TO5 = "AcdTol_E_1to5";
	private static final String TRAIT_ASI = "ASI";
	private static final String TRAIT_APHID = "Aphid1_5";
	private static final String TRAIT_EPH = "EPH";
	private static final String TRAIT_FMSROT = "FMSROT";
	private static final String[] TRAITS = {
		BreedingViewImportServiceImplTest.TRAIT_ASI, BreedingViewImportServiceImplTest.TRAIT_APHID,
		BreedingViewImportServiceImplTest.TRAIT_EPH, BreedingViewImportServiceImplTest.TRAIT_FMSROT};
	private static final String ANALYSIS_VAR_NAME = BreedingViewImportServiceImplTest.TRAIT_ASI + MeansCSV.MEANS_SUFFIX;
	private static final int STUDY_ID = 1;
	private static final String STUDY_NAME = "TEST STUDY";

	private static final int MEASUREMENT_DATASET_ID = 2;
	private static final int EXISTING_MEANS_DATASET_ID = 3;
	private static final int NEW_MEANS_DATASET_ID = 4;
	private static final int TRIAL_DATASET_ID = 1;
	private static final String PROGRAM_UUID = "12345678";
	private static final Integer TEST_ANALYSIS_VARIABLE_TERM_ID = 65000;

	private static final String EMPTY_VALUE = "";

	private static final String LS_MEAN = "LS MEAN";
	private static final Integer LS_MEAN_ID = 16090;
	public static final String LOCATION_NAME = "LOCATION_NAME";
	public static final String TRIAL_INSTANCE = "TRIAL_INSTANCE";

	private final List<DMSVariableType> factorVariableTypes = new ArrayList<>();
	private final List<DMSVariableType> variateVariableTypes = new ArrayList<>();
	private final List<DMSVariableType> meansVariateVariableTypes = new ArrayList<>();
	private final List<DMSVariableType> summaryVariableTypes = new ArrayList<>();

	private final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private DaoFactory daoFactory;

	@Mock
	private OntologyScaleDataManager scaleDataManager;

	@Mock
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Mock
	private OntologyMethodDataManager methodDataManager;

	@Mock
	private StandardVariableTransformer standardVariableTransformer;

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private TrialEnvironments environments;

	@Mock
	private SummaryStatsCSV summaryStatsCSV;

	@Mock
	private ContextUtil contextUtil;

	@Captor
	private ArgumentCaptor<List<ExperimentValues>> experimentValuesCaptor;

	private CVTerm meansCVTerm;

	@InjectMocks
	private final BreedingViewImportServiceImpl bvImportService = new BreedingViewImportServiceImpl();

	private Stocks stocks;
	private CropType crop;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		this.messageSource.setBasename("CommonMessages");

		final CVTermDao cvTermDao = Mockito.mock(CVTermDao.class);
		Mockito.doReturn(cvTermDao).when(this.daoFactory).getCvTermDao();

		this.meansCVTerm = this.createCVTerm(BreedingViewImportServiceImplTest.LS_MEAN_ID, BreedingViewImportServiceImplTest.LS_MEAN);
		Mockito.doReturn(this.meansCVTerm).when(cvTermDao)
			.getByNameAndCvId(BreedingViewImportServiceImplTest.LS_MEAN, CvId.METHODS.getId());

		Mockito.doReturn(this.createDmsProject(BreedingViewImportServiceImplTest.STUDY_ID, BreedingViewImportServiceImplTest.STUDY_NAME,
			BreedingViewImportServiceImplTest.PROGRAM_UUID)).when(this.studyDataManager)
			.getProject(BreedingViewImportServiceImplTest.STUDY_ID);

		this.factorVariableTypes.add(this.createEnvironmentVariableType(TRIAL_INSTANCE));
		this.factorVariableTypes.add(this.createGermplasmFactorVariableType("ENTRY_NO", TermId.ENTRY_NO.getId()));
		this.factorVariableTypes.add(this.createGermplasmFactorVariableType("GID", TermId.GID.getId()));

		for (final String traitName : BreedingViewImportServiceImplTest.TRAITS) {
			this.variateVariableTypes.add(this.createVariateVariableType(traitName));
			this.meansVariateVariableTypes.add(this.createVariateVariableType(traitName + MeansCSV.MEANS_SUFFIX, "", "", "LS MEAN"));
			this.meansVariateVariableTypes
				.add(this.createVariateVariableType(traitName + MeansCSV.UNIT_ERRORS_SUFFIX, "", "", "ERROR ESTIMATE"));
		}

		Mockito.doReturn(this.createMeasurementDataSet()).when(this.studyDataManager)
			.findOneDataSetByType(BreedingViewImportServiceImplTest.STUDY_ID, DatasetTypeEnum.PLOT_DATA.getId());
		Mockito.doReturn(this.createDataSet()).when(this.studyDataManager)
			.findOneDataSetByType(BreedingViewImportServiceImplTest.STUDY_ID, DatasetTypeEnum.SUMMARY_DATA.getId());

		this.stocks = new Stocks();
		Mockito.when(this.studyDataManager.getStocksInDataset(ArgumentMatchers.anyInt())).thenReturn(this.stocks);

		this.bvImportService.setCloner(new Cloner());
		this.bvImportService.setMessageSource(this.messageSource);

		Mockito.doReturn(new StandardVariable()).when(this.standardVariableTransformer)
			.transformVariable(ArgumentMatchers.<org.generationcp.middleware.domain.ontology.Variable>any());

		Mockito.doReturn(new StandardVariable()).when(this.standardVariableTransformer)
			.transformVariable(ArgumentMatchers.<org.generationcp.middleware.domain.ontology.Variable>isNull());

		Mockito.when(this.scaleDataManager.getScaleById(ArgumentMatchers.anyInt(), ArgumentMatchers.anyBoolean()))
			.thenReturn(OntologyScaleTestDataInitializer.createScale());

		final Project workbenchProject = new Project();
		this.crop = new CropType();
		this.crop.setCropName("maize");
		workbenchProject.setCropType(this.crop);
		Mockito.doReturn(workbenchProject).when(this.contextUtil).getProjectInContext();
	}

	private DmsProject createDmsProject(final int id, final String name, final String programUUID) {
		final DmsProject study = new DmsProject();
		study.setProjectId(id);
		study.setName(name);
		study.setProgramUUID(programUUID);
		return study;
	}

	@Test
	public void testImportMeansData() throws Exception {
		// import with no existing means data
		Mockito.doReturn(null).when(this.studyDataManager)
			.findOneDataSetByType(BreedingViewImportServiceImplTest.STUDY_ID, DatasetTypeEnum.MEANS_DATA.getId());

		Mockito.when(this.studyDataManager
			.addDataSet(ArgumentMatchers.anyInt(), ArgumentMatchers.<VariableTypeList>any(), ArgumentMatchers.<DatasetValues>any(),
				ArgumentMatchers.anyString(), Mockito.anyInt())).thenReturn(new DatasetReference(
			BreedingViewImportServiceImplTest.NEW_MEANS_DATASET_ID,
			BreedingViewImportServiceImplTest.EMPTY_VALUE));

		Mockito.when(this.studyDataManager.getDataSet(BreedingViewImportServiceImplTest.NEW_MEANS_DATASET_ID))
			.thenReturn(this.createNewMeansDataSet());

		Mockito.doReturn(null).when(this.ontologyVariableDataManager).getWithFilter(ArgumentMatchers.<VariableFilter>any());
		Mockito.doNothing().when(this.ontologyVariableDataManager).addVariable(ArgumentMatchers.<OntologyVariableInfo>any());

		Mockito.when(this.studyDataManager.getTrialEnvironmentsInDataset(ArgumentMatchers.anyInt())).thenReturn(this.environments);

		final File file = new File(ClassLoader.getSystemClassLoader().getResource("BMSOutput.csv").toURI());

		this.bvImportService.importMeansData(file, BreedingViewImportServiceImplTest.STUDY_ID);

		Mockito.verify(this.studyDataManager)
			.addDataSet(ArgumentMatchers.eq(BreedingViewImportServiceImplTest.STUDY_ID), ArgumentMatchers.<VariableTypeList>any(),
				ArgumentMatchers.<DatasetValues>any(),
				ArgumentMatchers.eq(BreedingViewImportServiceImplTest.PROGRAM_UUID), ArgumentMatchers.eq(DatasetTypeEnum.MEANS_DATA.getId()));

		Mockito.verify(this.studyDataManager)
			.addOrUpdateExperiment(ArgumentMatchers.eq(this.crop),
				ArgumentMatchers.eq(BreedingViewImportServiceImplTest.NEW_MEANS_DATASET_ID), ArgumentMatchers.eq(ExperimentType.AVERAGE),
				ArgumentMatchers.<ExperimentValues>anyList());

	}

	private CVTerm createCVTerm(final int cvTermId, final String name) {
		final CVTerm cvTerm = new CVTerm();
		cvTerm.setCvTermId(cvTermId);
		cvTerm.setName(name);
		return cvTerm;
	}

	@Test
	public void testImportMeansDataWithExistingMeansDataSet() throws Exception {
		// import with existing means data
		Mockito.doReturn(this.createExistingMeansDataSet()).when(this.studyDataManager)
			.findOneDataSetByType(BreedingViewImportServiceImplTest.STUDY_ID, DatasetTypeEnum.MEANS_DATA.getId());

		Mockito.doReturn(null).when(this.ontologyVariableDataManager).getWithFilter(ArgumentMatchers.<VariableFilter>any());
		Mockito.doNothing().when(this.ontologyVariableDataManager).addVariable(ArgumentMatchers.<OntologyVariableInfo>any());

		Mockito.when(this.studyDataManager.getTrialEnvironmentsInDataset(ArgumentMatchers.anyInt())).thenReturn(this.environments);

		Mockito.doNothing().when(this.studyDataManager)
			.addDataSetVariableType(ArgumentMatchers.anyInt(), ArgumentMatchers.<DMSVariableType>any());

		final File file = new File(ClassLoader.getSystemClassLoader().getResource("BMSOutput.csv").toURI());

		this.bvImportService.importMeansData(file, BreedingViewImportServiceImplTest.STUDY_ID);

		Mockito.verify(this.studyDataManager, Mockito.times(0))
			.addDataSetVariableType(ArgumentMatchers.anyInt(), ArgumentMatchers.<DMSVariableType>any());
		Mockito.verify(this.studyDataManager)
			.addOrUpdateExperiment(ArgumentMatchers.eq(this.crop),
				ArgumentMatchers.eq(BreedingViewImportServiceImplTest.EXISTING_MEANS_DATASET_ID),
				ArgumentMatchers.eq(ExperimentType.AVERAGE), ArgumentMatchers.<ExperimentValues>anyList());
	}

	@Test
	public void testImportMeansDataWithExistingMeansDataAndAdditionalTraits() throws Exception {
		this.variateVariableTypes.add(this.createVariateVariableType("EXTRAIT"));
		// Recreate plot dataset since new variate was added
		Mockito.doReturn(this.createMeasurementDataSet()).when(this.studyDataManager)
			.findOneDataSetByType(BreedingViewImportServiceImplTest.STUDY_ID, DatasetTypeEnum.PLOT_DATA.getId());

		Mockito.doReturn(this.createExistingMeansDataSet()).when(this.studyDataManager)
			.findOneDataSetByType(BreedingViewImportServiceImplTest.STUDY_ID, DatasetTypeEnum.MEANS_DATA.getId());

		Mockito.when(this.studyDataManager.getTrialEnvironmentsInDataset(ArgumentMatchers.anyInt())).thenReturn(this.environments);

		Mockito.doNothing().when(this.studyDataManager)
			.addDataSetVariableType(ArgumentMatchers.anyInt(), ArgumentMatchers.<DMSVariableType>any());

		Mockito.doReturn(null).when(this.ontologyVariableDataManager).getWithFilter(ArgumentMatchers.<VariableFilter>any());
		Mockito.doNothing().when(this.ontologyVariableDataManager).addVariable(ArgumentMatchers.<OntologyVariableInfo>any());

		final File file = new File(ClassLoader.getSystemClassLoader().getResource("BMSOutputWithAdditionalTraits.csv").toURI());

		this.bvImportService.importMeansData(file, BreedingViewImportServiceImplTest.STUDY_ID);

		Mockito.verify(this.studyDataManager, Mockito.times(1))
			.addDataSetVariableType(ArgumentMatchers.anyInt(), ArgumentMatchers.<DMSVariableType>any());
		Mockito.verify(this.studyDataManager)
			.addOrUpdateExperiment(ArgumentMatchers.eq(this.crop),
				ArgumentMatchers.eq(BreedingViewImportServiceImplTest.EXISTING_MEANS_DATASET_ID),
				ArgumentMatchers.eq(ExperimentType.AVERAGE), ArgumentMatchers.<ExperimentValues>anyList());

	}

	@Test
	public void testImportOutlierData() throws Exception {

		final List<Object[]> phenotypeIds = new ArrayList<>();
		phenotypeIds.add(new Object[] {"76373", "9999", "1"});

		Mockito.when(this.studyDataManager.getTrialEnvironmentsInDataset(ArgumentMatchers.anyInt())).thenReturn(this.createEnvironments());
		Mockito.when(this.studyDataManager
			.getPhenotypeIdsByLocationAndPlotNo(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(),
				ArgumentMatchers.<Integer>anyList())).thenReturn(phenotypeIds);

		final File file = new File(ClassLoader.getSystemClassLoader().getResource("BMSOutlier.csv").toURI());
		this.bvImportService.importOutlierData(file, BreedingViewImportServiceImplTest.STUDY_ID);

		Mockito.verify(this.studyDataManager).saveOrUpdatePhenotypeOutliers(ArgumentMatchers.<PhenotypeOutlier>anyList());
	}

	@Test
	public void testImportSummaryStatsData() throws Exception {

		final DataSet dataSet = this.createDataSet();
		Mockito.doReturn(dataSet).when(this.studyDataManager)
			.findOneDataSetByType(BreedingViewImportServiceImplTest.STUDY_ID, DatasetTypeEnum.SUMMARY_DATA.getId());

		Mockito.when(this.studyDataManager.getTrialEnvironmentsInDataset(ArgumentMatchers.anyInt())).thenReturn(this.createEnvironments());

		final CVTermDao cvTermDao = Mockito.mock(CVTermDao.class);
		Mockito.doReturn(cvTermDao).when(this.daoFactory).getCvTermDao();
		Mockito.doReturn(this.createCVTerm(888, "DUMMYTERM")).when(cvTermDao).getByNameAndCvId("DUMMYTERM", CvId.METHODS.getId());

		Mockito.doReturn(null).when(this.ontologyVariableDataManager).getWithFilter(ArgumentMatchers.<VariableFilter>any());
		Mockito.doNothing().when(this.ontologyVariableDataManager).addVariable(ArgumentMatchers.<OntologyVariableInfo>any());
		Mockito.when(this.studyDataManager.isLocationIdVariable(BreedingViewImportServiceImplTest.STUDY_ID, "TRIAL_INSTANCE"))
			.thenReturn(false);
		Mockito.when(this.studyDataManager.createInstanceLocationIdToNameMapFromStudy(BreedingViewImportServiceImplTest.STUDY_ID))
			.thenReturn(HashBiMap.<String, String>create());

		final File file = new File(ClassLoader.getSystemClassLoader().getResource("BMSSummary.csv").toURI());
		this.bvImportService.importSummaryStatsData(file, BreedingViewImportServiceImplTest.STUDY_ID);

		final ArgumentCaptor<DmsProject> datasetCaptor = ArgumentCaptor.forClass(DmsProject.class);
		final ArgumentCaptor<VariableTypeList> variableTypeListCaptor = ArgumentCaptor.forClass(VariableTypeList.class);
		Mockito.verify(this.studyDataManager)
			.saveTrialDatasetSummary(datasetCaptor.capture(), variableTypeListCaptor.capture(), this.experimentValuesCaptor.capture(),
				ArgumentMatchers.<Integer>anyList());
		// Check arguments used when updating trial dataset
		Assert.assertEquals(Integer.valueOf(dataSet.getId()), datasetCaptor.getValue().getProjectId());
		final int numberOfSummaryVariables = BreedingViewImportServiceImplTest.TRAITS.length * SummaryStatsCSV.SUMMARY_STATS_METHODS.length;
		Assert.assertEquals(numberOfSummaryVariables, variableTypeListCaptor.getValue().getVariableTypes().size());
		Assert.assertEquals(numberOfSummaryVariables, this.experimentValuesCaptor.getValue().size());

	}

	private DataSet createDataSet() {

		final DataSet dataSet = new DataSet();
		dataSet.setId(BreedingViewImportServiceImplTest.TRIAL_DATASET_ID);

		final VariableTypeList variableTypes = new VariableTypeList();

		dataSet.setVariableTypes(variableTypes);
		for (final DMSVariableType factor : this.factorVariableTypes) {
			dataSet.getVariableTypes().add(factor);
		}
		for (final DMSVariableType variate : this.variateVariableTypes) {
			dataSet.getVariableTypes().add(variate);
		}

		return dataSet;
	}

	private DataSet createMeasurementDataSet() {

		final DataSet dataSet = new DataSet();
		dataSet.setId(BreedingViewImportServiceImplTest.MEASUREMENT_DATASET_ID);

		final VariableTypeList variableTypes = new VariableTypeList();

		dataSet.setVariableTypes(variableTypes);
		for (final DMSVariableType factor : this.factorVariableTypes) {
			dataSet.getVariableTypes().add(factor);
		}
		for (final DMSVariableType variate : this.variateVariableTypes) {
			dataSet.getVariableTypes().add(variate);
		}

		return dataSet;
	}

	private DataSet createNewMeansDataSet() {

		final DataSet dataSet = new DataSet();
		dataSet.setId(BreedingViewImportServiceImplTest.NEW_MEANS_DATASET_ID);

		final VariableTypeList variableTypes = new VariableTypeList();

		dataSet.setVariableTypes(variableTypes);
		for (final DMSVariableType factor : this.factorVariableTypes) {
			dataSet.getVariableTypes().add(factor);
		}
		for (final DMSVariableType variate : this.meansVariateVariableTypes) {
			dataSet.getVariableTypes().add(variate);
		}

		return dataSet;
	}

	private DataSet createExistingMeansDataSet() {

		final DataSet dataSet = new DataSet();
		dataSet.setId(BreedingViewImportServiceImplTest.EXISTING_MEANS_DATASET_ID);

		final VariableTypeList variableTypes = new VariableTypeList();

		dataSet.setVariableTypes(variableTypes);
		for (final DMSVariableType factor : this.factorVariableTypes) {
			dataSet.getVariableTypes().add(factor);
		}
		for (final DMSVariableType variate : this.meansVariateVariableTypes) {
			dataSet.getVariableTypes().add(variate);
		}

		return dataSet;
	}

	private DMSVariableType createVariateVariableType(final String localName) {
		final DMSVariableType variate = new DMSVariableType();
		final StandardVariable variateStandardVar = new StandardVariable();
		variateStandardVar.setPhenotypicType(PhenotypicType.VARIATE);

		final Term storedIn = new Term();
		storedIn.setId(TermId.OBSERVATION_VARIATE.getId());

		final Term dataType = new Term();
		dataType.setId(TermId.NUMERIC_VARIABLE.getId());

		final Term method = new Term();
		method.setId(1111);
		method.setDefinition(BreedingViewImportServiceImplTest.EMPTY_VALUE);

		final Term scale = new Term();
		scale.setDefinition(BreedingViewImportServiceImplTest.EMPTY_VALUE);
		scale.setId(22222);

		final Term property = new Term();
		scale.setDefinition(BreedingViewImportServiceImplTest.EMPTY_VALUE);
		scale.setId(33333);

		variateStandardVar.setId(1234);
		variateStandardVar.setProperty(property);
		variateStandardVar.setScale(scale);
		variateStandardVar.setMethod(method);
		variateStandardVar.setDataType(dataType);
		variateStandardVar.setName(localName);
		variate.setLocalName(localName);
		variate.setStandardVariable(variateStandardVar);
		variate.setRole(variateStandardVar.getPhenotypicType());

		return variate;
	}

	private DMSVariableType createVariateVariableType(
		final String localName, final String propertyName, final String scaleName,
		final String methodName) {
		final DMSVariableType variate = new DMSVariableType();
		final StandardVariable variateStandardVar = new StandardVariable();
		variateStandardVar.setPhenotypicType(PhenotypicType.VARIATE);

		final Term storedIn = new Term();
		storedIn.setId(TermId.OBSERVATION_VARIATE.getId());

		final Term dataType = new Term();
		dataType.setId(TermId.NUMERIC_VARIABLE.getId());

		final Term method = new Term();
		method.setId(1111);
		method.setDefinition(BreedingViewImportServiceImplTest.EMPTY_VALUE);
		method.setName(methodName);

		final Term scale = new Term();
		scale.setDefinition(BreedingViewImportServiceImplTest.EMPTY_VALUE);
		scale.setId(22222);
		scale.setName(scaleName);

		final Term property = new Term();
		property.setDefinition(BreedingViewImportServiceImplTest.EMPTY_VALUE);
		property.setId(33333);
		property.setName(propertyName);

		variateStandardVar.setId(1234);
		variateStandardVar.setProperty(property);
		variateStandardVar.setScale(scale);
		variateStandardVar.setMethod(method);
		variateStandardVar.setDataType(dataType);
		variate.setLocalName(localName);
		variate.setStandardVariable(variateStandardVar);
		variate.setRole(variateStandardVar.getPhenotypicType());

		return variate;
	}

	private DMSVariableType createEnvironmentVariableType(final String localName) {

		final DMSVariableType factor = new DMSVariableType();
		final StandardVariable factorStandardVar = new StandardVariable();
		factorStandardVar.setId(TermId.LOCATION_ID.getId());
		factorStandardVar.setPhenotypicType(PhenotypicType.TRIAL_ENVIRONMENT);
		factorStandardVar.setName(localName);
		factor.setLocalName(localName);
		factor.setStandardVariable(factorStandardVar);
		factor.setRole(factorStandardVar.getPhenotypicType());
		return factor;
	}

	private DMSVariableType createAnalysisSummaryVariableType(final String localName) {
		final DMSVariableType factor = new DMSVariableType();
		final StandardVariable factorStandardVar = new StandardVariable();
		factorStandardVar.setPhenotypicType(PhenotypicType.VARIATE);
		factorStandardVar.setName(localName);
		factor.setLocalName(localName);
		factor.setStandardVariable(factorStandardVar);
		factor.setRole(factorStandardVar.getPhenotypicType());
		factor.setVariableType(VariableType.ANALYSIS_SUMMARY);
		return factor;
	}

	private DMSVariableType createGermplasmFactorVariableType(final String localName, final int termId) {
		final DMSVariableType factor = new DMSVariableType();
		final StandardVariable factorStandardVar = new StandardVariable();
		factorStandardVar.setPhenotypicType(PhenotypicType.GERMPLASM);

		final Term dataType = new Term();
		dataType.setId(TermId.NUMERIC_DBID_VARIABLE.getId());

		final Term method = new Term();
		method.setId(1111);
		method.setDefinition(BreedingViewImportServiceImplTest.EMPTY_VALUE);

		final Term scale = new Term();
		scale.setDefinition(BreedingViewImportServiceImplTest.EMPTY_VALUE);
		scale.setId(22222);

		final Term property = new Term();
		scale.setDefinition(BreedingViewImportServiceImplTest.EMPTY_VALUE);
		scale.setId(33333);

		factorStandardVar.setId(termId);
		factorStandardVar.setProperty(property);
		factorStandardVar.setScale(scale);
		factorStandardVar.setMethod(method);
		factorStandardVar.setDataType(dataType);
		factor.setLocalName(localName);
		factor.setStandardVariable(factorStandardVar);
		factor.setRole(factorStandardVar.getPhenotypicType());

		return factor;
	}

	private TrialEnvironments createEnvironments() {

		final TrialEnvironments environmentsEnvs = new TrialEnvironments();
		environmentsEnvs.add(this.createEnvironment(1));
		return environmentsEnvs;

	}

	private TrialEnvironment createEnvironment(final int geolocationId) {

		final LocationDto location = new LocationDto(geolocationId, "CIMMYT");
		final VariableList variableList = new VariableList();

		this.addVariables(variableList);

		final TrialEnvironment environment = new TrialEnvironment(geolocationId, variableList);
		environment.setLocation(location);

		return environment;

	}

	private void addVariables(final VariableList list) {
		for (final DMSVariableType f : this.factorVariableTypes) {
			list.add(this.createVariable(f));
		}
		for (final DMSVariableType v : this.variateVariableTypes) {
			list.add(this.createVariable(v));
		}
	}

	private Variable createVariable(final DMSVariableType variableType) {
		final Variable v = new Variable();
		if (variableType.getLocalName().equals("TRIAL_INSTANCE")) {
			v.setValue("1");
		} else {
			v.setValue("2222");
		}

		v.setVariableType(variableType);
		return v;
	}

	@Test
	public void testImportMeansDataWithDupeEntryNo() throws Exception {
		// import with no existing means data
		Mockito.doReturn(null).when(this.studyDataManager)
			.findOneDataSetByType(BreedingViewImportServiceImplTest.STUDY_ID, DatasetTypeEnum.MEANS_DATA.getId());

		Mockito.when(this.studyDataManager
			.addDataSet(ArgumentMatchers.anyInt(), ArgumentMatchers.<VariableTypeList>any(), ArgumentMatchers.<DatasetValues>any(),
				ArgumentMatchers.anyString(), ArgumentMatchers.anyInt())).thenReturn(new DatasetReference(
			BreedingViewImportServiceImplTest.NEW_MEANS_DATASET_ID,
			BreedingViewImportServiceImplTest.EMPTY_VALUE));

		Mockito.when(this.studyDataManager.getDataSet(BreedingViewImportServiceImplTest.NEW_MEANS_DATASET_ID))
			.thenReturn(this.createNewMeansDataSet());

		Mockito.doReturn(null).when(this.ontologyVariableDataManager).getWithFilter(ArgumentMatchers.<VariableFilter>any());
		Mockito.doNothing().when(this.ontologyVariableDataManager).addVariable(ArgumentMatchers.<OntologyVariableInfo>any());

		Mockito.when(this.studyDataManager.getTrialEnvironmentsInDataset(ArgumentMatchers.anyInt())).thenReturn(this.environments);

		final File file = new File(ClassLoader.getSystemClassLoader().getResource("BMSOutputDupeEntryNo.csv").toURI());

		this.bvImportService.importMeansData(file, BreedingViewImportServiceImplTest.STUDY_ID);

		Mockito.verify(this.studyDataManager)
			.addDataSet(ArgumentMatchers.anyInt(), ArgumentMatchers.<VariableTypeList>any(), ArgumentMatchers.<DatasetValues>any(),
				ArgumentMatchers.anyString(), ArgumentMatchers.anyInt());

		Mockito.verify(this.studyDataManager)
			.addOrUpdateExperiment(ArgumentMatchers.eq(this.crop), ArgumentMatchers.anyInt(), ArgumentMatchers.<ExperimentType>any(),
				ArgumentMatchers.<ExperimentValues>anyList());

	}

	@Test
	public void testCreateAnalysisVariableNonExisting() {
		final boolean isSummaryVariable = false;

		final DMSVariableType originalVariableType = this.createVariateVariableType(BreedingViewImportServiceImplTest.TRAIT_ASI);
		final Term meansMethod = new Term();
		meansMethod.setId(BreedingViewImportServiceImplTest.LS_MEAN_ID);
		Mockito.when(this.ontologyDataManager
			.retrieveDerivedAnalysisVariable(originalVariableType.getId(), BreedingViewImportServiceImplTest.LS_MEAN_ID))
			.thenReturn(null);

		this.bvImportService.createAnalysisVariable(originalVariableType, BreedingViewImportServiceImplTest.ANALYSIS_VAR_NAME, meansMethod,
			BreedingViewImportServiceImplTest.PROGRAM_UUID, 1, isSummaryVariable);

		// Verify saving actions in Middleware
		final ArgumentCaptor<OntologyVariableInfo> infoArgument = ArgumentCaptor.forClass(OntologyVariableInfo.class);
		Mockito.verify(this.ontologyVariableDataManager).addVariable(infoArgument.capture());
		// anyInt is used as the 2nd argument since this represents the dynamic
		// term ID that will be generated after saving the new variable
		// into the DB
		Mockito.verify(this.ontologyDataManager)
			.addCvTermRelationship(ArgumentMatchers.eq(originalVariableType.getId()), ArgumentMatchers.anyInt(),
				ArgumentMatchers.eq(TermId.HAS_ANALYSIS_VARIABLE.getId()));
		final OntologyVariableInfo argument = infoArgument.getValue();

		Assert.assertEquals("Unable to properly add a new analysis variable with the proper method ID",
			BreedingViewImportServiceImplTest.LS_MEAN_ID, argument.getMethodId());
		Assert.assertEquals("Unable to properly add a new analysis variable with the proper name",
			BreedingViewImportServiceImplTest.ANALYSIS_VAR_NAME, argument.getName());
		Assert.assertNotNull(argument.getVariableTypes());
		Assert.assertEquals("Expecting only one variable type for new analysis variable but had more than one.", 1,
			argument.getVariableTypes().size());
		Assert.assertEquals("Expecting analysis variable to have variable type 'Analysis' but did not.", VariableType.ANALYSIS,
			argument.getVariableTypes().iterator().next());
	}

	@Test
	public void testCreateAnalysisVariableExisting() {
		final boolean isSummaryVariable = false;
		final DMSVariableType originalVariableType = this.createVariateVariableType(BreedingViewImportServiceImplTest.TRAIT_ASI);
		final Term meansMethod = new Term();
		meansMethod.setId(BreedingViewImportServiceImplTest.LS_MEAN_ID);

		Mockito.when(this.ontologyDataManager
			.retrieveDerivedAnalysisVariable(originalVariableType.getId(), BreedingViewImportServiceImplTest.LS_MEAN_ID))
			.thenReturn(BreedingViewImportServiceImplTest.TEST_ANALYSIS_VARIABLE_TERM_ID);

		this.bvImportService.createAnalysisVariable(originalVariableType, BreedingViewImportServiceImplTest.ANALYSIS_VAR_NAME, meansMethod,
			BreedingViewImportServiceImplTest.PROGRAM_UUID, 1, isSummaryVariable);

		Mockito.verify(this.ontologyVariableDataManager, Mockito.never()).addVariable(ArgumentMatchers.<OntologyVariableInfo>any());
		Mockito.verify(this.ontologyDataManager, Mockito.never())
			.addCvTermRelationship(ArgumentMatchers.eq(originalVariableType.getId()), ArgumentMatchers.anyInt(),
				ArgumentMatchers.eq(TermId.HAS_ANALYSIS_VARIABLE.getId()));

	}

	@Test
	public void testCreateAnalysisSummaryVariableNonExisting() {
		final boolean isSummaryVariable = true;
		final String analysisSummaryVariableName = BreedingViewImportServiceImplTest.TRAIT_ASI + "_NumMissing";
		final Integer methodId = 4130;

		final DMSVariableType originalVariableType = this.createVariateVariableType(BreedingViewImportServiceImplTest.TRAIT_ASI);
		final Term meansMethod = new Term();
		meansMethod.setId(methodId);
		Mockito.when(this.ontologyDataManager.retrieveDerivedAnalysisVariable(originalVariableType.getId(), methodId)).thenReturn(null);

		this.bvImportService.createAnalysisVariable(originalVariableType, analysisSummaryVariableName, meansMethod,
			BreedingViewImportServiceImplTest.PROGRAM_UUID, 1, isSummaryVariable);

		// Verify saving actions in Middleware
		final ArgumentCaptor<OntologyVariableInfo> infoArgument = ArgumentCaptor.forClass(OntologyVariableInfo.class);
		Mockito.verify(this.ontologyVariableDataManager).addVariable(infoArgument.capture());
		// anyInt is used as the 2nd argument since this represents the dynamic
		// term ID that will be generated after saving the new variable
		// into the DB
		Mockito.verify(this.ontologyDataManager)
			.addCvTermRelationship(ArgumentMatchers.eq(originalVariableType.getId()), ArgumentMatchers.anyInt(),
				ArgumentMatchers.eq(TermId.HAS_ANALYSIS_VARIABLE.getId()));
		final OntologyVariableInfo argument = infoArgument.getValue();

		Assert.assertEquals("Unable to properly add a new analysis summary variable with the proper method ID", methodId,
			argument.getMethodId());
		Assert.assertEquals("Unable to properly add a new analysis summary variable with the proper name", analysisSummaryVariableName,
			argument.getName());
		Assert.assertNotNull(argument.getVariableTypes());
		Assert.assertEquals("Expecting only one variable type for new analysis summary variable but had more than one.", 1,
			argument.getVariableTypes().size());
		Assert.assertEquals("Expecting analysis summary variable to have variable type 'Analysis Summary' but did not.",
			VariableType.ANALYSIS_SUMMARY, argument.getVariableTypes().iterator().next());
	}

	@Test
	public void testCreateMeansVariablesFromImportFile() {
		// Setup test data from file - 4 traits with 2 analysis variables each
		// (_Means and _ErrorEstimate suffixes)
		final VariableTypeList meansVariableList = new VariableTypeList();
		final List<String> csvHeaders = new ArrayList<>();
		for (final DMSVariableType factor : this.factorVariableTypes) {
			meansVariableList.add(factor);
			csvHeaders.add(factor.getLocalName());
		}
		final int oldVariableListSize = meansVariableList.size();
		final VariableTypeList plotVariateList = new VariableTypeList();
		for (final DMSVariableType trait : this.variateVariableTypes) {
			plotVariateList.add(trait);
		}
		for (final DMSVariableType analysisVar : this.meansVariateVariableTypes) {
			csvHeaders.add(analysisVar.getLocalName());
		}
		Mockito.when(this.ontologyDataManager.retrieveDerivedAnalysisVariable(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt()))
			.thenReturn(null);

		// Method to test
		final String[] csvHeadersArray = csvHeaders.toArray(new String[csvHeaders.size()]);
		this.bvImportService.createMeansVariablesFromImportFileAndAddToList(csvHeadersArray, plotVariateList, meansVariableList,
			BreedingViewImportServiceImplTest.PROGRAM_UUID, this.meansCVTerm, false);

		// Expecting 1 analysis variable for each trait: <trait name>_Means
		final int newVariablesSize = BreedingViewImportServiceImplTest.TRAITS.length;
		Assert.assertEquals("Expecting " + newVariablesSize + " analysis variables to be added to means dataset variables.",
			oldVariableListSize + newVariablesSize, meansVariableList.size());
		for (final String traitName : BreedingViewImportServiceImplTest.TRAITS) {
			boolean isMeansVarFound = false;
			for (final DMSVariableType trait : meansVariableList.getVariates().getVariableTypes()) {
				final String meansVariableName = traitName + MeansCSV.MEANS_SUFFIX;
				if (meansVariableName.equals(trait.getLocalName())) {
					isMeansVarFound = true;
					continue;
				}
			}
			Assert.assertTrue("Expecting means analysis variable for " + traitName + " was added but was not.", isMeansVarFound);
		}
		// Check that new variables were added to have "Analysis" variable type
		final ArgumentCaptor<OntologyVariableInfo> infoArgument = ArgumentCaptor.forClass(OntologyVariableInfo.class);
		Mockito.verify(this.ontologyVariableDataManager, Mockito.times(newVariablesSize)).addVariable(infoArgument.capture());
		for (final OntologyVariableInfo variableInfo : infoArgument.getAllValues()) {
			Assert.assertEquals(1, variableInfo.getVariableTypes().size());
			Assert.assertEquals("Expecting 'Analysis' as sole variable type for new variable.", VariableType.ANALYSIS,
				variableInfo.getVariableTypes().iterator().next());
		}
	}

	@Test
	public void testAppendVariableTypesToExistingMeans() {
		// Setup test data from file - 4 traits with 2 analysis variables each
		// (_Means and _ErrorEstimate suffixes)
		final String[] prevAnalyzedTraits = {BreedingViewImportServiceImplTest.TRAIT_ASI, BreedingViewImportServiceImplTest.TRAIT_EPH};
		final VariableTypeList meansVariableList = new VariableTypeList();
		final List<String> csvHeaders = new ArrayList<String>();
		for (final DMSVariableType factor : this.factorVariableTypes) {
			meansVariableList.add(factor);
			csvHeaders.add(factor.getLocalName());
		}
		// Assuming there is existing means dataset with 2 traits previously
		// analyzed
		for (final String traitName : prevAnalyzedTraits) {
			meansVariableList.add(this.createVariateVariableType(traitName + MeansCSV.MEANS_SUFFIX, "", "", "LS MEAN"));
		}
		final int oldVariableListSize = meansVariableList.size();

		final DataSet plotDataSet = new DataSet();
		plotDataSet.setId(BreedingViewImportServiceImplTest.MEASUREMENT_DATASET_ID);
		final VariableTypeList plotVariateList = new VariableTypeList();
		for (final DMSVariableType trait : this.variateVariableTypes) {
			plotVariateList.add(trait);
		}
		plotDataSet.setVariableTypes(plotVariateList);

		final DataSet meansDataSet = new DataSet();
		meansDataSet.setId(BreedingViewImportServiceImplTest.EXISTING_MEANS_DATASET_ID);
		for (final DMSVariableType analysisVar : this.meansVariateVariableTypes) {
			csvHeaders.add(analysisVar.getLocalName());
		}
		meansDataSet.setVariableTypes(meansVariableList);
		Mockito.when(this.ontologyDataManager.retrieveDerivedAnalysisVariable(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt()))
			.thenReturn(null);

		// Method to test
		final String[] csvHeadersArray = csvHeaders.toArray(new String[csvHeaders.size()]);
		this.bvImportService.appendVariableTypesToExistingMeans(csvHeadersArray, plotDataSet, meansDataSet,
			BreedingViewImportServiceImplTest.PROGRAM_UUID, this.meansCVTerm, false);

		// Expecting 1 analysis variable for each unanalyzed trait: <trait
		// name>_Means
		final int newVariablesSize = BreedingViewImportServiceImplTest.TRAITS.length - prevAnalyzedTraits.length;
		Assert.assertEquals("Expecting " + newVariablesSize + " analysis variables to be added to means dataset variables.",
			oldVariableListSize + newVariablesSize, meansVariableList.size());
		for (final String traitName : BreedingViewImportServiceImplTest.TRAITS) {
			boolean isMeansVarFound = false;
			for (final DMSVariableType trait : meansVariableList.getVariates().getVariableTypes()) {
				final String meansVariableName = traitName + MeansCSV.MEANS_SUFFIX;
				if (meansVariableName.equals(trait.getLocalName())) {
					isMeansVarFound = true;
					continue;
				}
			}
			Assert.assertTrue("Expecting means analysis variable for " + traitName + " but was not found.", isMeansVarFound);
		}
		final ArgumentCaptor<Integer> datasetIdArgument = ArgumentCaptor.forClass(Integer.class);
		Mockito.verify(this.studyDataManager, Mockito.times(newVariablesSize))
			.addDataSetVariableType(datasetIdArgument.capture(), ArgumentMatchers.<DMSVariableType>any());
		Assert.assertEquals("Expecting correct ID to be used for adding new variables to means dataset.",
			BreedingViewImportServiceImplTest.EXISTING_MEANS_DATASET_ID, datasetIdArgument.getValue().intValue());

		// Check that new variables were added to have "Analysis" variable type
		final ArgumentCaptor<OntologyVariableInfo> infoArgument = ArgumentCaptor.forClass(OntologyVariableInfo.class);
		Mockito.verify(this.ontologyVariableDataManager, Mockito.times(newVariablesSize)).addVariable(infoArgument.capture());
		final List<String> prevAnalyzedTraitsList = Arrays.asList(prevAnalyzedTraits);
		for (final OntologyVariableInfo variableInfo : infoArgument.getAllValues()) {
			Assert.assertEquals(1, variableInfo.getVariableTypes().size());
			Assert.assertEquals("Expecting 'Analysis' as sole variable type for new variable.", VariableType.ANALYSIS,
				variableInfo.getVariableTypes().iterator().next());
			final String analysisVariableName = variableInfo.getName();
			final String sourceTraitName = analysisVariableName.substring(0, analysisVariableName.lastIndexOf("_"));
			Assert.assertFalse(
				"Expecting analysis variables are not added for previously analyzed traits.",
				prevAnalyzedTraitsList.contains(sourceTraitName));
		}
	}

	@Test
	public void testCreateSummaryStatsVariableTypes() throws IOException {
		this.setUpSummaryStatsData();

		final DataSet dataSet = new DataSet();
		dataSet.setId(BreedingViewImportServiceImplTest.MEASUREMENT_DATASET_ID);
		final VariableTypeList variableTypeList = new VariableTypeList();
		variableTypeList.add(this.createEnvironmentVariableType("TRIAL_INSTANCE"));
		dataSet.setVariableTypes(variableTypeList);

		final VariableTypeList plotVariateList = new VariableTypeList();
		final Map<String, String> traitAliasMap = new HashMap<>();
		for (final DMSVariableType trait : this.variateVariableTypes) {
			plotVariateList.add(trait);
			traitAliasMap.put(trait.getLocalName(), trait.getLocalName());
		}
		this.bvImportService.setLocalNameToAliasMap(traitAliasMap);
		// Not all traits in plot dataset were analyzed (only two included in
		// summary file)
		final List<String> traitsAnalyzed =
			Arrays.asList(BreedingViewImportServiceImplTest.TRAITS[0], BreedingViewImportServiceImplTest.TRAITS[1]);
		Mockito.when(this.summaryStatsCSV.getTraits()).thenReturn(traitsAnalyzed);
		Mockito.when(this.ontologyDataManager.retrieveDerivedAnalysisVariable(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt()))
			.thenReturn(null);

		// Method to test
		final VariableTypeList summaryStatVariables = this.bvImportService
			.createSummaryStatsVariableTypes(this.summaryStatsCSV, dataSet, plotVariateList,
				BreedingViewImportServiceImplTest.PROGRAM_UUID);

		// Expecting one analysis summary variable per summary statistic method
		// for each trait
		final List<String> expectedSummaryVariableNames = new ArrayList<>();
		for (final String trait : traitsAnalyzed) {
			for (final String method : this.summaryStatsCSV.getSummaryHeaders()) {
				expectedSummaryVariableNames.add(trait + "_" + method);
			}
		}
		Assert.assertEquals("Expecting " + this.summaryStatsCSV.getSummaryHeaders().size() + " summary statistics variables per trait.",
			expectedSummaryVariableNames.size(), summaryStatVariables.size());

		// Check that variable type "Analysis Summary" was used for new summary
		// statistic variables
		for (final DMSVariableType variable : summaryStatVariables.getVariableTypes()) {
			Assert.assertEquals("Expecting 'Analysis Summary' variable type for added variable.", VariableType.ANALYSIS_SUMMARY,
				variable.getVariableType());

		}
		final ArgumentCaptor<OntologyVariableInfo> infoArgument = ArgumentCaptor.forClass(OntologyVariableInfo.class);
		Mockito.verify(this.ontologyVariableDataManager, Mockito.times(expectedSummaryVariableNames.size()))
			.addVariable(infoArgument.capture());
		for (final OntologyVariableInfo variableInfo : infoArgument.getAllValues()) {
			Assert.assertEquals(1, variableInfo.getVariableTypes().size());
			Assert.assertEquals("Expecting 'Analysis Summary' as sole variable type for new variable.", VariableType.ANALYSIS_SUMMARY,
				variableInfo.getVariableTypes().iterator().next());
		}

	}

	@Test
	public void testCreateSummaryStatsVariableTypesExistingAlreadyInTrialDataset() throws IOException {
		this.setUpSummaryStatsData();

		final VariableTypeList plotVariateList = new VariableTypeList();
		final Map<String, String> traitAliasMap = new HashMap<>();
		for (final DMSVariableType trait : this.variateVariableTypes) {
			plotVariateList.add(trait);
			traitAliasMap.put(trait.getLocalName(), trait.getLocalName());
		}
		this.bvImportService.setLocalNameToAliasMap(traitAliasMap);
		final DataSet dataSet = new DataSet();
		dataSet.setId(BreedingViewImportServiceImplTest.MEASUREMENT_DATASET_ID);
		final VariableTypeList variableTypeList = new VariableTypeList();
		variableTypeList.add(this.createEnvironmentVariableType("TRIAL_INSTANCE"));
		// Add summary analysis variables for all traits in study dataset so
		// there will be none left to create
		for (final String trait : traitAliasMap.keySet()) {
			for (final String summaryHeader : this.summaryStatsCSV.getSummaryHeaders()) {
				final String varName = trait + "_" + summaryHeader;
				variableTypeList.add(this.createAnalysisSummaryVariableType(varName));
			}
		}
		dataSet.setVariableTypes(variableTypeList);
		Mockito.when(this.ontologyDataManager.retrieveDerivedAnalysisVariable(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt()))
			.thenReturn(1);

		// Method to test
		final VariableTypeList summaryStatVariables = this.bvImportService
			.createSummaryStatsVariableTypes(this.summaryStatsCSV, dataSet, plotVariateList,
				BreedingViewImportServiceImplTest.PROGRAM_UUID);

		// Check that no new summary variables saved and returned
		Assert.assertEquals("Expecting no new summary statistics variables created.", 0, summaryStatVariables.size());
		Mockito.verify(this.ontologyVariableDataManager, Mockito.never()).addVariable(ArgumentMatchers.<OntologyVariableInfo>any());
		Mockito.verify(this.ontologyDataManager, Mockito.never())
			.addCvTermRelationship(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(),
				ArgumentMatchers.eq(TermId.HAS_ANALYSIS_VARIABLE.getId()));

	}

	private void setUpSummaryStatsData() throws IOException {
		for (final String trait : BreedingViewImportServiceImplTest.TRAITS) {
			for (final String summaryHeader : SummaryStatsCSV.SUMMARY_STATS_METHODS) {
				this.summaryVariableTypes.add(this.createVariateVariableType(trait + "_" + summaryHeader));
			}
		}

		final Map<String, Map<String, List<String>>> data = SummaryStatsTestDataInitializer.generateData();
		final List<String> summaryHeaders = Arrays.asList(SummaryStatsCSV.SUMMARY_STATS_METHODS);
		Mockito.when(this.summaryStatsCSV.getSummaryHeaders()).thenReturn(summaryHeaders);
		Mockito.when(this.summaryStatsCSV.getData()).thenReturn(data);
		Mockito.when(this.summaryStatsCSV.getTraits()).thenReturn(Arrays.asList(BreedingViewImportServiceImplTest.TRAITS));
	}

	@Test
	public void testCreateSummaryStatsExperimentValuesList() throws IOException {
		this.setUpSummaryStatsData();
		final Map<String, Map<String, List<String>>> data = this.summaryStatsCSV.getData();
		final List<String> summaryHeaders = this.summaryStatsCSV.getSummaryHeaders();

		final DataSet dataSet = new DataSet();
		final VariableTypeList varTypeList = new VariableTypeList();
		varTypeList.setVariableTypes(this.summaryVariableTypes);
		dataSet.setVariableTypes(varTypeList);

		final Map<Integer, String> locationNameToIdMap = new LinkedHashMap<>();
		for (final String environment : data.keySet()) {
			locationNameToIdMap.put(Integer.valueOf(environment), environment);
		}

		// Method to test
		final List<ExperimentValues> experimentValues =
			this.bvImportService.createSummaryStatsExperimentValuesList(dataSet, locationNameToIdMap, this.summaryStatsCSV);

		// Check the count and value of generated ExperimentValues list
		final int numberOfEnvironments = data.keySet().size();
		Assert.assertEquals(
			numberOfEnvironments * summaryHeaders.size() * SummaryStatsTestDataInitializer.TRAITS_LIST.size(),
			experimentValues.size());
		final Iterator<ExperimentValues> actualValuesIterator = experimentValues.iterator();
		for (final String environmentName : data.keySet()) {
			for (int i = 1; i <= summaryHeaders.size(); i++) {
				for (int j = 0; j < SummaryStatsTestDataInitializer.TRAITS_LIST.size(); j++) {
					final String trait = SummaryStatsTestDataInitializer.TRAITS_LIST.get(j);
					final String summaryVariableName = trait + "_" + summaryHeaders.get(i - 1);
					final Integer value = Integer.valueOf(environmentName) * 10 + i;

					final ExperimentValues experimentValue = actualValuesIterator.next();
					Assert.assertEquals(environmentName, experimentValue.getLocationId().toString());
					final Variable variable = experimentValue.getVariableList().getVariables().get(0);
					Assert.assertEquals(summaryVariableName, variable.getVariableType().getLocalName());
					Assert.assertEquals(value.toString() + "." + j, variable.getActualValue());
				}
			}
		}
	}

	@Test
	public void testGenerateNameToAliasMapWhenPreviousMapIsNotEmpth() {
		final Map<String, String> aliasMap = new HashMap<>();
		for (final String trait : SummaryStatsTestDataInitializer.TRAITS_LIST) {
			aliasMap.put(trait, trait + "_");
		}
		this.bvImportService.setLocalNameToAliasMap(aliasMap);
		final DataSet plotDataSet = Mockito.mock(DataSet.class);

		// Method to test
		this.bvImportService.generateNameToAliasMap(plotDataSet);

		final Map<String, String> finalAliasMap = this.bvImportService.getLocalNameToAliasMap();
		Assert.assertEquals(aliasMap, finalAliasMap);
		Mockito.verify(plotDataSet, Mockito.never()).getVariableTypes();
	}

	@Test
	public void testGenerateNameToAliasMapWhenPreviousMapIsEmpty() {
		this.bvImportService.setLocalNameToAliasMap(new HashMap<String, String>());
		final VariableTypeList varTypeList = new VariableTypeList();
		for (final DMSVariableType factor : this.factorVariableTypes) {
			varTypeList.add(factor);
		}
		for (final DMSVariableType trait : this.variateVariableTypes) {
			varTypeList.add(trait);
		}
		final DataSet plotDataSet = new DataSet();
		plotDataSet.setVariableTypes(varTypeList);

		// Method to test
		this.bvImportService.generateNameToAliasMap(plotDataSet);

		// final alias map will contain ENTRY_NO_1 for dupe entry no column from
		// BV
		final Map<String, String> finalAliasMap = this.bvImportService.getLocalNameToAliasMap();
		Assert.assertEquals(varTypeList.size() + 1, finalAliasMap.size());
		for (final DMSVariableType varType : varTypeList.getVariableTypes()) {
			Assert.assertTrue(finalAliasMap.keySet().contains(varType.getLocalName()));
			Assert.assertTrue(finalAliasMap.containsValue(varType.getLocalName()));
		}
	}

	@Test
	@Ignore()
	public void testCreateGeolocationIdEnvironmentMapEnvironmentFactorIsLocationIDVariable() {

		this.factorVariableTypes.add(this.createEnvironmentVariableType(BreedingViewImportServiceImplTest.LOCATION_NAME));

		final int testGeolocationId1 = 100;
		final String testLocationName1 = "Agua Fria (AF)";
		final String testLocationId1 = "1001";

		final int testGeolocationId2 = 101;
		final String testLocationName2 = "Africa";
		final String testLocationId2 = "1002";

		final int testGeolocationId3 = 102;
		final String testLocationName3 = "UP Los Banos, Philippines";
		final String testLocationId3 = "1003";

		final TrialEnvironments testEnvironments = new TrialEnvironments();

		final TrialEnvironment environment1 = this.createEnvironment(testGeolocationId1);
		environment1.getVariables().findByLocalName(BreedingViewImportServiceImplTest.LOCATION_NAME).setValue(testLocationId1);
		final TrialEnvironment environment2 = this.createEnvironment(testGeolocationId2);
		environment2.getVariables().findByLocalName(BreedingViewImportServiceImplTest.LOCATION_NAME).setValue(testLocationId2);
		final TrialEnvironment environment3 = this.createEnvironment(testGeolocationId3);
		environment3.getVariables().findByLocalName(BreedingViewImportServiceImplTest.LOCATION_NAME).setValue(testLocationId3);

		testEnvironments.add(environment1);
		testEnvironments.add(environment2);
		testEnvironments.add(environment3);

		final BiMap<String, String> locationMap = HashBiMap.create();
		locationMap.put(testLocationId1, testLocationName1);
		locationMap.put(testLocationId2, testLocationName2);
		locationMap.put(testLocationId3, testLocationName3);

		Mockito.when(this.studyDataManager.getTrialEnvironmentsInDataset(ArgumentMatchers.anyInt())).thenReturn(testEnvironments);
		Mockito.when(this.studyDataManager.createInstanceLocationIdToNameMapFromStudy(ArgumentMatchers.anyInt())).thenReturn(locationMap);
		Mockito.when(this.studyDataManager.isLocationIdVariable(ArgumentMatchers.anyInt(), ArgumentMatchers.anyString())).thenReturn(true);

		// Only add environments 1 and 3
		final Set<String> environments = new HashSet<>();
		environments.add(testLocationName1);
		environments.add(testLocationName3);

		final Map<Integer, String> result = this.bvImportService
			.createGeolocationIdEnvironmentMap(environments, BreedingViewImportServiceImplTest.STUDY_ID,
				BreedingViewImportServiceImplTest.LOCATION_NAME);

		// Verify that only environments 1 and 3 are in the
		// geolocationIDEnvironmentMap
		Assert.assertEquals(result.get(testGeolocationId1), testLocationName1);
		Assert.assertEquals(result.get(testGeolocationId3), testLocationName3);
		Assert.assertFalse(result.containsKey(testGeolocationId2));

	}

	@Test
	public void testCreateGeolocationIdEnvironmentMapEnvironmentFactorIsTrialInstance() {

		final int testGeolocationId1 = 100;
		final String testTrialInstance1 = "1";

		final int testGeolocationId2 = 101;
		final String testTrialInstance2 = "2";

		final int testGeolocationId3 = 102;
		final String testTrialInstance3 = "3";

		final TrialEnvironments testEnvironments = new TrialEnvironments();

		final TrialEnvironment environment1 = this.createEnvironment(testGeolocationId1);
		environment1.getVariables().findByLocalName(BreedingViewImportServiceImplTest.TRIAL_INSTANCE).setValue(testTrialInstance1);
		final TrialEnvironment environment2 = this.createEnvironment(testGeolocationId2);
		environment2.getVariables().findByLocalName(BreedingViewImportServiceImplTest.TRIAL_INSTANCE).setValue(testTrialInstance2);
		final TrialEnvironment environment3 = this.createEnvironment(testGeolocationId3);
		environment3.getVariables().findByLocalName(BreedingViewImportServiceImplTest.TRIAL_INSTANCE).setValue(testTrialInstance3);

		testEnvironments.add(environment1);
		testEnvironments.add(environment2);
		testEnvironments.add(environment3);

		final BiMap<String, String> locationMap = HashBiMap.create();

		Mockito.when(this.studyDataManager.getTrialEnvironmentsInDataset(ArgumentMatchers.anyInt())).thenReturn(testEnvironments);
		Mockito.when(this.studyDataManager.createInstanceLocationIdToNameMapFromStudy(ArgumentMatchers.anyInt())).thenReturn(locationMap);
		Mockito.when(this.studyDataManager.isLocationIdVariable(ArgumentMatchers.anyInt(), ArgumentMatchers.anyString())).thenReturn(false);

		// Only add environments 1 and 3
		final Set<String> environments = new HashSet<>();
		environments.add(testTrialInstance1);
		environments.add(testTrialInstance3);

		final Map<Integer, String> result = this.bvImportService
			.createGeolocationIdEnvironmentMap(environments, BreedingViewImportServiceImplTest.STUDY_ID,
				BreedingViewImportServiceImplTest.TRIAL_INSTANCE);

		// Verify that only environments 1 and 3 are in the
		// geolocationIDEnvironmentMap
		Assert.assertEquals(result.get(testGeolocationId1), testTrialInstance1);
		Assert.assertEquals(result.get(testGeolocationId3), testTrialInstance3);
		Assert.assertFalse(result.containsKey(testTrialInstance2));

	}

	@Test
	public void testGenerateAnalysisVariableScaleNameForMeans() {

		final String variableName = BreedingViewImportServiceImplTest.ACDTOL_E_1TO5 + MeansCSV.MEANS_SUFFIX;

		final String scaleName = this.bvImportService.generateAnalysisVariableScaleName(variableName);
		Assert.assertEquals("Average AcdTol_E_1to5 Score", scaleName);
	}

	@Test
	public void testGenerateAnalysisVariableScaleNameForCV() {

		final String variableName = BreedingViewImportServiceImplTest.ACDTOL_E_1TO5 + BreedingViewImportServiceImpl.CV_SUFFIX;

		final String scaleName = this.bvImportService.generateAnalysisVariableScaleName(variableName);
		Assert.assertEquals("Percent SE/Mean for AcdTol_E_1to5", scaleName);
	}

	@Test
	public void testGenerateAnalysisVariableScaleNameForHeritability() {

		final String variableName = BreedingViewImportServiceImplTest.ACDTOL_E_1TO5 + BreedingViewImportServiceImpl.HERITABILITY_SUFFIX;

		final String scaleName = this.bvImportService.generateAnalysisVariableScaleName(variableName);
		Assert.assertEquals("Ratio genetic variance/phenotypic variance for variable AcdTol_E_1to5", scaleName);
	}

	@Test
	public void testGenerateAnalysisVariableScaleNameForPValue() {

		final String variableName = BreedingViewImportServiceImplTest.ACDTOL_E_1TO5 + BreedingViewImportServiceImpl.PVALUE_SUFFIX;

		final String scaleName = this.bvImportService.generateAnalysisVariableScaleName(variableName);
		Assert.assertEquals("Significance of test for mean differences for variable AcdTol_E_1to5", scaleName);
	}

	@Test
	public void testGetAnalysisVariableScaleIdWhereScaleIsNumeric() {
		final Scale scale = OntologyScaleTestDataInitializer.createScale();
		final int scaleId = this.bvImportService.getAnalysisVariableScaleId(scale.getId(), scale.getName());
		Assert.assertEquals(scale.getId(), scaleId);
		Mockito.verify(this.scaleDataManager).getScaleById(scale.getId(), true);
		Mockito.verify(this.ontologyDataManager, Mockito.never())
			.findTermByName(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CvId.SCALES));
	}

	@Test
	public void testGetAnalysisVariableScaleIdWhereScaleIsCategoricalAndNonExistent() {
		final String variableName = BreedingViewImportServiceImplTest.ACDTOL_E_1TO5 + MeansCSV.MEANS_SUFFIX;

		final Scale scale = OntologyScaleTestDataInitializer.createScaleWithNameAndDataType(variableName, DataType.CATEGORICAL_VARIABLE);
		Mockito.when(this.scaleDataManager.getScaleById(ArgumentMatchers.anyInt(), ArgumentMatchers.anyBoolean())).thenReturn(scale);

		this.bvImportService.getAnalysisVariableScaleId(scale.getId(), variableName);
		Mockito.verify(this.scaleDataManager).getScaleById(scale.getId(), true);
		Mockito.verify(this.ontologyDataManager)
			.findTermByName(this.bvImportService.generateAnalysisVariableScaleName(variableName), CvId.SCALES);
		Mockito.verify(this.scaleDataManager).addScale(ArgumentMatchers.<Scale>any());
	}

	@Test
	public void testGetAnalysisVariableScaleIdWhereScaleIsCategoricalAndExisting() {

		final String variableName = BreedingViewImportServiceImplTest.ACDTOL_E_1TO5 + MeansCSV.MEANS_SUFFIX;

		final Scale scale = OntologyScaleTestDataInitializer.createScaleWithNameAndDataType(variableName, DataType.CATEGORICAL_VARIABLE);
		Mockito.when(this.scaleDataManager.getScaleById(ArgumentMatchers.anyInt(), ArgumentMatchers.anyBoolean())).thenReturn(scale);
		final String scaleName = this.bvImportService.generateAnalysisVariableScaleName(variableName);
		Mockito.when(this.ontologyDataManager.findTermByName(scaleName, CvId.SCALES)).thenReturn(scale);

		this.bvImportService.getAnalysisVariableScaleId(scale.getId(), variableName);
		Mockito.verify(this.scaleDataManager).getScaleById(scale.getId(), true);
		Mockito.verify(this.ontologyDataManager).findTermByName(scaleName, CvId.SCALES);
		Mockito.verify(this.scaleDataManager, Mockito.never()).addScale(ArgumentMatchers.<Scale>any());
	}

	@Test
	public void testCreateEnvironmentNameToNdGeolocationIdMapEnvironmentFactorIsLocationIDVariable() {

		this.factorVariableTypes.add(this.createEnvironmentVariableType(BreedingViewImportServiceImplTest.LOCATION_NAME));

		final Integer testGeolocationId1 = 100;
		final String testLocationName1 = "Agua Fria (AF)";
		final String testLocationId1 = "1001";

		final Integer testGeolocationId2 = 101;
		final String testLocationName2 = "Africa";
		final String testLocationId2 = "1002";

		final TrialEnvironment environment1 = this.createEnvironment(testGeolocationId1);
		environment1.getVariables().findByLocalName(BreedingViewImportServiceImplTest.LOCATION_NAME).setValue(testLocationId1);
		final TrialEnvironment environment2 = this.createEnvironment(testGeolocationId2);
		environment2.getVariables().findByLocalName(BreedingViewImportServiceImplTest.LOCATION_NAME).setValue(testLocationId2);

		final TrialEnvironments testEnvironments = new TrialEnvironments();
		testEnvironments.add(environment1);
		testEnvironments.add(environment2);

		final BiMap<String, String> locationMap = HashBiMap.create();
		locationMap.put(testLocationId1, testLocationName1);
		locationMap.put(testLocationId2, testLocationName2);

		Mockito.when(this.studyDataManager.getTrialEnvironmentsInDataset(ArgumentMatchers.anyInt())).thenReturn(testEnvironments);
		Mockito.when(this.studyDataManager.isLocationIdVariable(STUDY_ID, LOCATION_NAME)).thenReturn(true);
		Mockito.when(this.studyDataManager.createInstanceLocationIdToNameMapFromStudy(STUDY_ID)).thenReturn(locationMap);

		final Map<String, Integer> result =
			this.bvImportService.createEnvironmentNameToNdGeolocationIdMap(LOCATION_NAME, STUDY_ID, TRIAL_DATASET_ID);

		Assert.assertEquals(result.size(), testEnvironments.size());
		Assert.assertEquals(testGeolocationId1, result.get(testLocationName1));
		Assert.assertEquals(testGeolocationId2, result.get(testLocationName2));

	}

	@Test
	public void testCreateEnvironmentNameToNdGeolocationIdMapEnvironmentFactorIsTrialInstance() {

		final Integer testGeolocationId1 = 100;
		final String testTrialInstance1 = "1";

		final Integer testGeolocationId2 = 101;
		final String testTrialInstance2 = "2";

		final TrialEnvironment environment1 = this.createEnvironment(testGeolocationId1);
		environment1.getVariables().findByLocalName(TRIAL_INSTANCE).setValue(testTrialInstance1);
		final TrialEnvironment environment2 = this.createEnvironment(testGeolocationId2);
		environment2.getVariables().findByLocalName(TRIAL_INSTANCE).setValue(testTrialInstance2);

		final TrialEnvironments testEnvironments = new TrialEnvironments();
		testEnvironments.add(environment1);
		testEnvironments.add(environment2);

		final BiMap<String, String> locationMap = HashBiMap.create();

		Mockito.when(this.studyDataManager.getTrialEnvironmentsInDataset(ArgumentMatchers.anyInt())).thenReturn(testEnvironments);
		Mockito.when(this.studyDataManager.isLocationIdVariable(STUDY_ID, TRIAL_INSTANCE)).thenReturn(false);
		Mockito.when(this.studyDataManager.createInstanceLocationIdToNameMapFromStudy(STUDY_ID)).thenReturn(locationMap);

		final Map<String, Integer> result =
			this.bvImportService.createEnvironmentNameToNdGeolocationIdMap(TRIAL_INSTANCE, STUDY_ID, TRIAL_DATASET_ID);

		Assert.assertEquals(result.size(), testEnvironments.size());
		Assert.assertEquals(testGeolocationId1, result.get(testTrialInstance1));
		Assert.assertEquals(testGeolocationId2, result.get(testTrialInstance2));

	}
}
