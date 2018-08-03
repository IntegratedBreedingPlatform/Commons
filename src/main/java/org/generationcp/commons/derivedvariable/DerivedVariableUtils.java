package org.generationcp.commons.derivedvariable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.ontology.FormulaVariable;

import java.math.BigDecimal;
import java.util.ArrayList;
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
	public static final String TERM_INSIDE_DELIMITERS_REGEX = TERM_LEFT_DELIMITER + "(.*?)" + TERM_RIGHT_DELIMITER;
	public static final Pattern TERM_INSIDE_DELIMITERS_PATTERN = Pattern.compile(TERM_INSIDE_DELIMITERS_REGEX);

	/**
	 * We use braces externally for clarity and replace them internally as they are map literals in jexl
	 */
	private static final String TERM_INTERNAL_DELIMITER = "__";

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
	 * @see DerivedVariableUtils#extractValues(Map, MeasurementRow, Set)
	 */
	public static void extractValues(final Map<String, Object> parameters, final MeasurementRow measurementRow) {
		extractValues(parameters, measurementRow, new HashSet<String>());
	}

	/**
	 * Extract values of parameters from the measurement
	 * @param termMissingData list to be filled with term labels with missing data
	 */
	public static void extractValues(
		final Map<String, Object> parameters, final MeasurementRow measurementRow, final Set<String> termMissingData) {

		if (measurementRow != null && measurementRow.getDataList() != null) {
			for (final MeasurementData measurementData : measurementRow.getDataList()) {
				String term = String.valueOf(measurementData.getMeasurementVariable().getTermId());
				term = StringUtils.deleteWhitespace(term);
				term = wrapTerm(term);
				if (parameters.containsKey(term)) {
					parameters.put(term, getMeasurementValue(measurementData, termMissingData));
				}
			}
		}
	}

	private static Object getMeasurementValue(final MeasurementData measurementData, final Set<String> termMissingData) {
		String value = null;
		if (!StringUtils.isBlank(measurementData.getcValueId())) {
			value = measurementData.getDisplayValueForCategoricalData().getName();
		}
		if (StringUtils.isBlank(value)) {
			value = measurementData.getValue();
		}
		if (StringUtils.isBlank(value) && termMissingData != null) {
			termMissingData.add(measurementData.getLabel());
		}
		if (NumberUtils.isNumber(value)) {
			return new BigDecimal(value);
		}
		return value;
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
	static String wrapTerm(final String term) {
		return TERM_INTERNAL_DELIMITER + term + TERM_INTERNAL_DELIMITER;
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
			String parameter = matcher.group(0);
			final String termId = matcher.group(1);
			parameter = StringUtils.trim(parameter);
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
			String parameter = matcher.group(0);
			final String name = matcher.group(1);
			parameter = StringUtils.trim(parameter);
			if (formulaVariableMap.containsKey(name)) {
				// Replace the name inside delimiters
				replaceText = StringUtils.replace(replaceText, name, String.valueOf(formulaVariableMap.get(name).getId()));
			}

		}
		return replaceText;
	}

}
