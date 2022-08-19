package org.generationcp.commons.derivedvariable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.FormulaVariable;
import org.generationcp.middleware.service.api.dataset.ObservationUnitData;
import org.generationcp.middleware.service.api.dataset.ObservationUnitRow;
import org.generationcp.middleware.util.Util;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DerivedVariableUtils {

	private static final String TERM_LEFT_DELIMITER = "\\{\\{";
	private static final String TERM_RIGHT_DELIMITER = "\\}\\}";
	private static final String TERM_INSIDE_DELIMITERS_REGEX = TERM_LEFT_DELIMITER + "(.*?)" + TERM_RIGHT_DELIMITER;
	private static final Pattern TERM_INSIDE_DELIMITERS_PATTERN = Pattern.compile(TERM_INSIDE_DELIMITERS_REGEX);
	// Just include any of the following after implementing the corresponding function: (COUNT|DISTINCT_COUNT|MAX|MIN)
	private static final String AGGREGATE_REGEX = "(avg|sum)\\((\\{\\{(\\w+)}})(,[\\s]?\\{\\{(\\w+)}})*\\)";
	private static final String AGGREGATE_ALL_INPUT_REGEX = "\\((\\{\\{(\\w+)}})(,[\\s]?\\{\\{(\\w+)}})*\\)";
	private static final String AGGREGATE_INPUT_REGEX = "(\\{\\{\\w+}})";
	public static final List<String> AGGREGATE_FUNCTIONS = Arrays.asList("avg", "sum");
	/**
	 * We use braces externally for clarity and replace them internally as they are map literals in jexl
	 */
	static final String TERM_INTERNAL_DELIMITER = "__";

	private DerivedVariableUtils() {
		// utility class
	}

	public static List<String> extractInputs(final String formula) {
		final List<String> inputVariables = new ArrayList<>();
		final Matcher matcher = TERM_INSIDE_DELIMITERS_PATTERN.matcher(formula);
		while (matcher.find()) {
			String term = matcher.group(1);
			term = StringUtils.deleteWhitespace(term);
			inputVariables.add(term);
		}
		return inputVariables;
	}

	/**
	 * Extract parameters from formula.
	 *
	 * @return map of parameters with internal delimiters to be evaluated by the formula engine
	 */
	public static Map<String, Object> extractParameters(final String formula) {
		final Map<String, Object> inputVariables = new HashMap<>();
		for (final String input : extractInputs(formula)) {
			inputVariables.put(wrapTerm(input), "");
		}
		return inputVariables;
	}

	/**
	 * Extract values of parameters from the measurement (ObservationUnitRow)
	 *
	 * @param parameters
	 * @param observationUnitRow
	 * @param measurementVariablesMap
	 * @param aggregateInputVariables
	 * @param environmentInputVariables
	 * @return list of term labels with missing data
	 * @throws ParseException
	 */
	public static Set<String> extractValues(
		final Map<String, Object> parameters, final ObservationUnitRow observationUnitRow,
		final Map<Integer, MeasurementVariable> measurementVariablesMap, final List<String> aggregateInputVariables,
		final List<String> environmentInputVariables) throws ParseException {

		final Set<String> termMissingData = new HashSet<>();

		if (observationUnitRow != null && observationUnitRow.getVariables() != null
			&& observationUnitRow.getEnvironmentVariables() != null) {
			for (final Map.Entry<String, Object> parameterItem : parameters.entrySet()) {
				final String termId = removeDelimiter(parameterItem.getKey());
				final String termName = measurementVariablesMap.get(Integer.valueOf(termId)).getName();
				final ObservationUnitData observationUnitData;

				// If a variable is present in both environment and observation levels, read the input variable data according
				// to where the user specified to. environmentInputVariables contains the ids of input variables that should be
				// read from environment level.
				if (environmentInputVariables.contains(termId)) {
					observationUnitData = observationUnitRow.getEnvironmentVariables().get(termName);
				} else {
					observationUnitData = observationUnitRow.getVariables().get(termName);
				}
				if (!aggregateInputVariables.contains(parameterItem.getKey())) {
					parameterItem.setValue(getMeasurementValue(observationUnitData, measurementVariablesMap, termMissingData));
				}
			}
		}

		return termMissingData;
	}

	private static Object getMeasurementValue(
		final ObservationUnitData observationUnitData, final Map<Integer, MeasurementVariable> measurementVariablesMap,
		final Set<String> termMissingData) throws ParseException {
		String value = null;

		final MeasurementVariable measurementVariable = measurementVariablesMap.get(observationUnitData.getVariableId());

		if (observationUnitData.getCategoricalValueId() != null) {
			value = getPossibleValueName(observationUnitData.getCategoricalValueId(), measurementVariable);
		}
		if (StringUtils.isBlank(value)) {
			value = observationUnitData.getValue();
		}
		return parseValue(value, measurementVariable, termMissingData);
	}

	public static Object parseValue(final Object valueToParse, final MeasurementVariable measurementVariable,
		final Set<String> termMissingData) throws ParseException {

		final String value = (String) valueToParse;

		if ((StringUtils.isBlank(value) || MeasurementData.MISSING_VALUE.equals(value)) && termMissingData != null) {
			termMissingData.add(measurementVariable.getLabel());
		}

		if (DataType.DATE_TIME_VARIABLE.getId().equals(measurementVariable.getDataTypeId())
			&& !StringUtils.isBlank(value)) {
			final Date dateISOFormat = Util.tryParseDate(value, Util.FRONTEND_DATE_FORMAT);
			if (null != dateISOFormat) {
				return dateISOFormat;
			}
			// we try one more format and else -> ParseException
			return DateUtil.parseDate(value);
		}
		if (NumberUtils.isNumber(value)) {
			return new BigDecimal(value);
		}
		return value;

	}

	public static List<Object> parseValueList(final List<Object> valueToParseList, final MeasurementVariable measurementVariable,
		final Set<String> termMissingData) throws ParseException {

		final List<Object> parsedValues = new ArrayList<>();
		for (final Object value : valueToParseList) {
			parsedValues.add(parseValue(value, measurementVariable, termMissingData));
		}
		return parsedValues;

	}

	private static String getPossibleValueName(final Integer cagetoricalValueId, final MeasurementVariable measurementVariable) {
		for (final ValueReference possibleValue : measurementVariable.getPossibleValues()) {
			if (possibleValue.getId().equals(cagetoricalValueId)) {
				return possibleValue.getName();
			}
		}
		return "";
	}

	/**
	 * @return formula with internal delimiters
	 */
	public static String replaceDelimiters(final String formula) {
		String updatedFormula = formula;
		if (updatedFormula != null) {
			updatedFormula = updatedFormula.replaceAll(TERM_LEFT_DELIMITER, TERM_INTERNAL_DELIMITER);
			updatedFormula = updatedFormula.replaceAll(TERM_RIGHT_DELIMITER, TERM_INTERNAL_DELIMITER);
		}
		return updatedFormula;
	}

	/**
	 * Wrap term to be used as engine parameter
	 */
	public static String wrapTerm(final String term) {
		return TERM_INTERNAL_DELIMITER + term + TERM_INTERNAL_DELIMITER;
	}

	private static String removeDelimiter(final String termWithDelimiter) {
		return StringUtils.removeEnd(StringUtils.removeStart(termWithDelimiter, TERM_INTERNAL_DELIMITER), TERM_INTERNAL_DELIMITER);
	}

	/**
	 * @param formulaVariableMap to retrieve variable names by term id
	 * @return the formula definition with variable names and <strong>no</strong> delimiters
	 */
	public static String getDisplayableFormat(final String formulaDefinition, final Map<String, FormulaVariable> formulaVariableMap) {
		String replaceText = formulaDefinition;
		final Matcher matcher = TERM_INSIDE_DELIMITERS_PATTERN.matcher(formulaDefinition);
		while (matcher.find()) {
			String parameter = matcher.group(0);
			final String termId = matcher.group(1);
			parameter = StringUtils.trim(parameter);
			if (formulaVariableMap.containsKey(termId)) {
				replaceText = StringUtils.replace(replaceText, parameter, formulaVariableMap.get(termId).getName());
			}

		}
		return replaceText;
	}

	/**
	 * @param formulaVariableMap to retrieve variable names by term id
	 * @return the formula definition with variable names and delimiters
	 */
	public static String getEditableFormat(final String formulaDefinition, final Map<String, FormulaVariable> formulaVariableMap) {
		String replaceText = formulaDefinition;
		final Matcher matcher = TERM_INSIDE_DELIMITERS_PATTERN.matcher(formulaDefinition);
		while (matcher.find()) {
			final String parameter = matcher.group(0);
			final String termId = matcher.group(1);
			if (formulaVariableMap.containsKey(termId)) {
				// Replace the termid inside delimiters
				replaceText = StringUtils.replace(replaceText, termId, formulaVariableMap.get(termId).getName());
			}

		}
		return replaceText;
	}

	/**
	 * @param formulaVariableMap to retrieve variable ids by name
	 * @return the formula definition with termids names and delimiters, to be stored in the database
	 */
	public static String getStorageFormat(final String formulaDefinition, final Map<String, FormulaVariable> formulaVariableMap) {
		String replaceText = formulaDefinition;
		final Matcher matcher = TERM_INSIDE_DELIMITERS_PATTERN.matcher(formulaDefinition);
		while (matcher.find()) {
			final String name = matcher.group(1);
			if (formulaVariableMap.containsKey(name)) {
				// Replace the name inside delimiters
				replaceText = StringUtils.replace(replaceText, name, String.valueOf(formulaVariableMap.get(name).getId()));
			}

		}
		return replaceText;
	}

	/**
	 * @param formula   - the formula
	 * @param isWrapped - defines whether the values would be wrapped or not
	 * @return the list of input variables inside the aggregate functions
	 */
	public static List<String> getAggregateFunctionInputVariables(final String formula, final boolean isWrapped) {
		final Pattern aggregatePattern = Pattern.compile(AGGREGATE_REGEX);
		final Matcher aggregateMatcher = aggregatePattern.matcher(formula);
		return extractAggregateInputVariables(isWrapped, aggregateMatcher);
	}

	public static Map<String, List<String>> getAggregateFunctionInputVariablesMap(final String formula) {
		final Map<String, List<String>> aggregateFunctionInputVariablesMap = new HashMap<>();
		for (final String aggregateFunction : AGGREGATE_FUNCTIONS) {
			final String aggregateRegex = "(" + aggregateFunction + ")" + AGGREGATE_ALL_INPUT_REGEX;
			final Pattern aggregatePattern = Pattern.compile(aggregateRegex);
			final Matcher aggregateMatcher = aggregatePattern.matcher(formula);
			final List<String> aggregateInputVariables = extractAggregateInputVariables(true, aggregateMatcher);
			aggregateFunctionInputVariablesMap.put(aggregateFunction, aggregateInputVariables);
		}
		return aggregateFunctionInputVariablesMap;
	}

	private static List<String> extractAggregateInputVariables(
		final boolean isWrapped, final Matcher aggregateMatcher) {
		final List<String> aggregateInputVariables = new ArrayList<>();
		while (aggregateMatcher.find()) {
			final String aggregateString = aggregateMatcher.group();
			final Pattern aggregateInputPattern = Pattern.compile(AGGREGATE_INPUT_REGEX);
			final Matcher aggregateInputMatcher = aggregateInputPattern.matcher(aggregateString);
			while (aggregateInputMatcher.find()) {
				String inputVariable = aggregateInputMatcher.group().replaceAll("(\\{)", "").replaceAll("}", "");
				inputVariable = isWrapped ? DerivedVariableUtils.wrapTerm(inputVariable) : inputVariable;
				aggregateInputVariables.add(inputVariable);
			}
		}
		return aggregateInputVariables;
	}

}
