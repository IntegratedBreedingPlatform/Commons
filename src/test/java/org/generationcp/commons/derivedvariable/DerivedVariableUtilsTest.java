package org.generationcp.commons.derivedvariable;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.service.api.dataset.ObservationUnitData;
import org.generationcp.middleware.service.api.dataset.ObservationUnitRow;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DerivedVariableUtilsTest {

	@Test
	public void testExtractValuesFromObservationUnitRowVariableIsCategorical() throws ParseException {

		final String variableName = RandomStringUtils.randomAlphanumeric(10);
		final int variableTermid = RandomUtils.nextInt();
		final int categoricalValueId = RandomUtils.nextInt();

		final Map<String, Object> parameters = new HashMap<>();
		final Set<String> termMissingData = new HashSet<>();
		parameters.put(DerivedVariableUtils.wrapTerm(String.valueOf(variableTermid)), null);

		final ObservationUnitRow observationUnitRow = new ObservationUnitRow();
		final Map<String, ObservationUnitData> observationUnitDataMap = new HashMap<>();
		observationUnitDataMap.put(variableName, this.createObservationUnitDataTestData(variableTermid, "1", categoricalValueId));
		observationUnitRow.setVariables(observationUnitDataMap);

		// Create Categorical Measurement Variable with Categorical Values. One of the categorical values matches the observation unit data.
		final Map<Integer, MeasurementVariable> measurementVariableMap = new HashMap<>();
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(variableTermid);
		measurementVariable.setLabel(variableName);
		measurementVariable.setDataTypeId(DataType.CATEGORICAL_VARIABLE.getId());
		final List<ValueReference> possibleValues = new ArrayList<>();
		possibleValues.add(new ValueReference(categoricalValueId, "ABC", "Value1"));
		measurementVariable.setPossibleValues(possibleValues);
		measurementVariableMap.put(variableTermid, measurementVariable);

		DerivedVariableUtils.extractValues(parameters, observationUnitRow, measurementVariableMap, termMissingData);

		assertEquals("ABC", parameters.get(DerivedVariableUtils.wrapTerm(String.valueOf(variableTermid))));

	}

	@Test
	public void testExtractValuesFromObservationUnitRowVariableIsCategoricalNoPossibleValuesMatched() throws ParseException {

		final String variableName = RandomStringUtils.randomAlphanumeric(10);
		final int variableTermid = RandomUtils.nextInt();

		final Map<String, Object> parameters = new HashMap<>();
		final Set<String> termMissingData = new HashSet<>();
		parameters.put(DerivedVariableUtils.wrapTerm(String.valueOf(variableTermid)), null);

		final ObservationUnitRow observationUnitRow = new ObservationUnitRow();
		final Map<String, ObservationUnitData> observationUnitDataMap = new HashMap<>();
		observationUnitDataMap.put(variableName, this.createObservationUnitDataTestData(variableTermid, "1", null));
		observationUnitRow.setVariables(observationUnitDataMap);

		// Create Categorical Measurement Variable with Categorical Values. One of the categorical values matches the observation unit data.
		final Map<Integer, MeasurementVariable> measurementVariableMap = new HashMap<>();
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(variableTermid);
		measurementVariable.setLabel(variableName);
		measurementVariable.setDataTypeId(DataType.CATEGORICAL_VARIABLE.getId());
		final List<ValueReference> possibleValues = new ArrayList<>();
		possibleValues.add(new ValueReference(RandomUtils.nextInt(), "ABC", "Value1"));
		measurementVariable.setPossibleValues(possibleValues);
		measurementVariableMap.put(variableTermid, measurementVariable);

		DerivedVariableUtils.extractValues(parameters, observationUnitRow, measurementVariableMap, termMissingData);

		assertEquals(new BigDecimal(1), parameters.get(DerivedVariableUtils.wrapTerm(String.valueOf(variableTermid))));

	}

	@Test
	public void testExtractValuesFromObservationUnitRowVariableIsDate() throws ParseException {

		final String variableName = RandomStringUtils.randomAlphanumeric(10);
		final int variableTermid = RandomUtils.nextInt();

		final Map<String, Object> parameters = new HashMap<>();
		final Set<String> termMissingData = new HashSet<>();
		parameters.put(DerivedVariableUtils.wrapTerm(String.valueOf(variableTermid)), null);

		final ObservationUnitRow observationUnitRow = new ObservationUnitRow();
		final Map<String, ObservationUnitData> observationUnitDataMap = new HashMap<>();
		observationUnitDataMap.put(variableName, this.createObservationUnitDataTestData(variableTermid, "20200101", null));
		observationUnitRow.setVariables(observationUnitDataMap);

		// Create Categorical Measurement Variable with Categorical Values. One of the categorical values matches the observation unit data.
		final Map<Integer, MeasurementVariable> measurementVariableMap = new HashMap<>();
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(variableTermid);
		measurementVariable.setLabel(variableName);
		measurementVariable.setDataTypeId(DataType.DATE_TIME_VARIABLE.getId());
		measurementVariableMap.put(variableTermid, measurementVariable);

		DerivedVariableUtils.extractValues(parameters, observationUnitRow, measurementVariableMap, termMissingData);

		assertEquals(DateUtil.parseDate("20200101"), parameters.get(DerivedVariableUtils.wrapTerm(String.valueOf(variableTermid))));

	}

	@Test(expected = ParseException.class)
	public void testExtractValuesFromObservationUnitRowVariableIsDateInvalid() throws ParseException {

		final String variableName = RandomStringUtils.randomAlphanumeric(10);
		final int variableTermid = RandomUtils.nextInt();

		final Map<String, Object> parameters = new HashMap<>();
		final Set<String> termMissingData = new HashSet<>();
		parameters.put(DerivedVariableUtils.wrapTerm(String.valueOf(variableTermid)), null);

		final ObservationUnitRow observationUnitRow = new ObservationUnitRow();
		final Map<String, ObservationUnitData> observationUnitDataMap = new HashMap<>();

		// Create invalid date observation unit data
		observationUnitDataMap.put(variableName, this.createObservationUnitDataTestData(variableTermid, "20200101456", null));
		observationUnitRow.setVariables(observationUnitDataMap);

		// Create Categorical Measurement Variable with Categorical Values. One of the categorical values matches the observation unit data.
		final Map<Integer, MeasurementVariable> measurementVariableMap = new HashMap<>();
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(variableTermid);
		measurementVariable.setLabel(variableName);
		measurementVariable.setDataTypeId(DataType.DATE_TIME_VARIABLE.getId());
		measurementVariableMap.put(variableTermid, measurementVariable);

		DerivedVariableUtils.extractValues(parameters, observationUnitRow, measurementVariableMap, termMissingData);

	}

	@Test
	public void testExtractValuesFromObservationUnitRowVariableIsNumeric() throws ParseException {

		final String variableName = RandomStringUtils.randomAlphanumeric(10);
		final int variableTermid = RandomUtils.nextInt();

		final Map<String, Object> parameters = new HashMap<>();
		final Set<String> termMissingData = new HashSet<>();
		parameters.put(DerivedVariableUtils.wrapTerm(String.valueOf(variableTermid)), null);

		final ObservationUnitRow observationUnitRow = new ObservationUnitRow();
		final Map<String, ObservationUnitData> observationUnitDataMap = new HashMap<>();

		observationUnitDataMap.put(variableName, this.createObservationUnitDataTestData(variableTermid, "123", null));
		observationUnitRow.setVariables(observationUnitDataMap);

		// Create Categorical Measurement Variable with Categorical Values. One of the categorical values matches the observation unit data.
		final Map<Integer, MeasurementVariable> measurementVariableMap = new HashMap<>();
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(variableTermid);
		measurementVariable.setLabel(variableName);
		measurementVariable.setDataTypeId(DataType.NUMERIC_VARIABLE.getId());
		measurementVariableMap.put(variableTermid, measurementVariable);

		DerivedVariableUtils.extractValues(parameters, observationUnitRow, measurementVariableMap, termMissingData);

		assertEquals(new BigDecimal(123), parameters.get(DerivedVariableUtils.wrapTerm(String.valueOf(variableTermid))));

	}

	@Test
	public void testExtractValuesFromObservationUnitRowVariableIsNumericButDataIsText() throws ParseException {

		final String variableName = RandomStringUtils.randomAlphanumeric(10);
		final int variableTermid = RandomUtils.nextInt();

		final Map<String, Object> parameters = new HashMap<>();
		final Set<String> termMissingData = new HashSet<>();
		parameters.put(DerivedVariableUtils.wrapTerm(String.valueOf(variableTermid)), null);

		final ObservationUnitRow observationUnitRow = new ObservationUnitRow();
		final Map<String, ObservationUnitData> observationUnitDataMap = new HashMap<>();

		observationUnitDataMap.put(variableName, this.createObservationUnitDataTestData(variableTermid, "ABC", null));
		observationUnitRow.setVariables(observationUnitDataMap);

		// Create Categorical Measurement Variable with Categorical Values. One of the categorical values matches the observation unit data.
		final Map<Integer, MeasurementVariable> measurementVariableMap = new HashMap<>();
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(variableTermid);
		measurementVariable.setLabel(variableName);
		measurementVariable.setDataTypeId(DataType.NUMERIC_VARIABLE.getId());
		measurementVariableMap.put(variableTermid, measurementVariable);

		DerivedVariableUtils.extractValues(parameters, observationUnitRow, measurementVariableMap, termMissingData);

		assertEquals("ABC", parameters.get(DerivedVariableUtils.wrapTerm(String.valueOf(variableTermid))));

	}

	@Test
	public void testExtractValuesFromObservationUnitRowMissingData() throws ParseException {

		final String variableName = RandomStringUtils.randomAlphanumeric(10);
		final int variableTermid = RandomUtils.nextInt();

		final Map<String, Object> parameters = new HashMap<>();
		final Set<String> termMissingData = new HashSet<>();
		parameters.put(DerivedVariableUtils.wrapTerm(String.valueOf(variableTermid)), null);

		final ObservationUnitRow observationUnitRow = new ObservationUnitRow();
		final Map<String, ObservationUnitData> observationUnitDataMap = new HashMap<>();

		observationUnitDataMap.put(variableName, this.createObservationUnitDataTestData(variableTermid, "", null));
		observationUnitRow.setVariables(observationUnitDataMap);

		// Create Categorical Measurement Variable with Categorical Values. One of the categorical values matches the observation unit data.
		final Map<Integer, MeasurementVariable> measurementVariableMap = new HashMap<>();
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(variableTermid);
		measurementVariable.setLabel(variableName);
		measurementVariable.setDataTypeId(DataType.NUMERIC_VARIABLE.getId());
		measurementVariableMap.put(variableTermid, measurementVariable);

		DerivedVariableUtils.extractValues(parameters, observationUnitRow, measurementVariableMap, termMissingData);

		assertEquals("", parameters.get(DerivedVariableUtils.wrapTerm(String.valueOf(variableTermid))));
		assertEquals(variableName, termMissingData.toArray()[0]);

	}

	@Test
	public void testParseValue_ValueIsNotNumeric() throws ParseException {

		final Set<String> termMissingData = new HashSet<>();
		final String variableName = RandomStringUtils.randomAlphanumeric(10);
		final int variableTermid = RandomUtils.nextInt();

		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(variableTermid);
		measurementVariable.setLabel(variableName);
		measurementVariable.setDataTypeId(DataType.CHARACTER_VARIABLE.getId());

		final Object result = DerivedVariableUtils.parseValue("Text", measurementVariable, termMissingData);

		assertTrue(result instanceof String);
		assertEquals("Text", result);

	}

	@Test
	public void testParseValue_ValueIsNumeric() throws ParseException {

		final Set<String> termMissingData = new HashSet<>();
		final String variableName = RandomStringUtils.randomAlphanumeric(10);
		final int variableTermid = RandomUtils.nextInt();

		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(variableTermid);
		measurementVariable.setLabel(variableName);
		measurementVariable.setDataTypeId(DataType.NUMERIC_VARIABLE.getId());

		final Object result = DerivedVariableUtils.parseValue("1", measurementVariable, termMissingData);

		assertTrue(result instanceof BigDecimal);
		assertEquals(new BigDecimal(1), result);

	}

	@Test
	public void testParseValue_ValueIsBlank() throws ParseException {

		final Set<String> termMissingData = new HashSet<>();
		final String variableName = RandomStringUtils.randomAlphanumeric(10);
		final int variableTermid = RandomUtils.nextInt();

		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(variableTermid);
		measurementVariable.setLabel(variableName);
		measurementVariable.setDataTypeId(DataType.NUMERIC_VARIABLE.getId());

		final Object result = DerivedVariableUtils.parseValue("", measurementVariable, termMissingData);

		assertTrue(result instanceof String);
		assertTrue(termMissingData.contains(variableName));

	}

	@Test()
	public void testParseValue_ValueIsDate() throws ParseException {

		final Set<String> termMissingData = new HashSet<>();
		final String variableName = RandomStringUtils.randomAlphanumeric(10);
		final int variableTermid = RandomUtils.nextInt();

		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(variableTermid);
		measurementVariable.setLabel(variableName);
		measurementVariable.setDataTypeId(DataType.DATE_TIME_VARIABLE.getId());

		final Object result = DerivedVariableUtils.parseValue("20180101", measurementVariable, termMissingData);
		assertTrue(result instanceof Date);

	}

	@Test
	public void testGetAggregateFunctionInputVariables() {
		String formula = "fn:avg({{1001}}, {{1002}}, {{1003}})";
		List<String> aggregateInputVariables = DerivedVariableUtils.getAggregateFunctionInputVariables(formula, false);
		List<String> aggregateInputVariablesWrapped = DerivedVariableUtils.getAggregateFunctionInputVariables(formula, true);
		for(int i=0; i<3; i ++) {
			Assert.assertEquals(String.valueOf(1001+i), aggregateInputVariables.get(i));
			Assert.assertEquals(DerivedVariableUtils.TERM_INTERNAL_DELIMITER + (1001+i) + DerivedVariableUtils.TERM_INTERNAL_DELIMITER, aggregateInputVariablesWrapped.get(i));
		}
	}

	@Test
	public void testParseValue_ValueIsDateButInvalid() {

		final Set<String> termMissingData = new HashSet<>();
		final String variableName = RandomStringUtils.randomAlphanumeric(10);
		final int variableTermid = RandomUtils.nextInt();

		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(variableTermid);
		measurementVariable.setLabel(variableName);
		measurementVariable.setDataTypeId(DataType.DATE_TIME_VARIABLE.getId());

		try {
			DerivedVariableUtils.parseValue("2002-99-99", measurementVariable, termMissingData);
			fail("parseValue should throw an error");
		} catch (final ParseException e) {
			// do nothing
		}

	}

	@Test
	public void testParseValueList() throws ParseException {

		final Set<String> termMissingData = new HashSet<>();
		final String variableName = RandomStringUtils.randomAlphanumeric(10);
		final int variableTermid = RandomUtils.nextInt();

		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(variableTermid);
		measurementVariable.setLabel(variableName);
		measurementVariable.setDataTypeId(DataType.NUMERIC_VARIABLE.getId());

		final List<Object> result = DerivedVariableUtils
			.parseValueList(Arrays.asList((Object) "1", (Object) "2", (Object) "3"), measurementVariable, termMissingData);

		assertTrue(result.contains(new BigDecimal(1)));
		assertTrue(result.contains(new BigDecimal(2)));
		assertTrue(result.contains(new BigDecimal(3)));

	}

	private ObservationUnitData createObservationUnitDataTestData(final Integer VARIABLE_ID, final String value, final Integer cValueId) {
		final ObservationUnitData observationUnitData = new ObservationUnitData();
		observationUnitData.setVariableId(VARIABLE_ID);
		observationUnitData.setValue(value);
		observationUnitData.setCategoricalValueId(cValueId);
		observationUnitData.setObservationId(RandomUtils.nextInt());
		return observationUnitData;
	}

}
