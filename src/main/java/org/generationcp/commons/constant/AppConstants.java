/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package org.generationcp.commons.constant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum AppConstants {

	// Label Printing
	SIZE_OF_PAPER_A4, SIZE_OF_PAPER_LETTER

	// Field Map
	, ROW_COLUMN, SERPENTINE, PLANTING_ORDER_ROW_COLUMN, PLANTING_ORDER_SERPENTINE

	// Study Manager

	, GERMPLASM_LIST_CHOOSE_SPECIFY_CHECK_SELECT_ONE_OR_MORE_IN_THE_LIST

	, NAMING_CONVENTION_CIMMYT_WHEAT, NAMING_CONVENTION_CIMMYT_MAIZE, NAMING_CONVENTION_OTHER_CROPS

	, SINGLE_PLANT_SELECTION_SF

	, METHOD_CHOICE_YES, METHOD_CHOICE_NO, LINE_CHOICE_YES, LINE_CHOICE_NO

	, SELECTED_BULK_SF, RANDOM_BULK_SF, RANDOM_BULK_CF, HALF_MASS_SELECTION, DOUBLE_HAPLOID_LINE

	, METHOD_UNKNOWN_DERIVATIVE_METHOD_SF, METHOD_UNKNOWN_GENERATIVE_METHOD_SF

	, ENTRY_CODE_PREFIX

	, NAME_SEPARATOR

	, GERMPLASM_LIST_TYPE_HARVEST, GERMPLASM_LIST_TYPE_GENERIC_LIST

	, TOOL_NAME_OLD_FIELDBOOK, TOOL_NAME_BREEDING_VIEW, PROGRAM_LISTS, CROP_LISTS, SAMPLE_LISTS

	, PRINCIPAL_INVESTIGATOR, LOCATION, OBJECTIVE

	, FEMALE_STUDY, FEMALE_ENTRY, FEMALE_PLOT, MALE_STUDY, MALE_ENTRY, MALE_PLOT, BREEDING_METHOD, CROSSING_DATE,
	SEEDS_HARVESTED, NOTES

	// Import Germplasm
	, CONDITION, DESCRIPTION, PROPERTY, SCALE, METHOD, DATA_TYPE, VALUE, FACTOR, ENTRY, DESIGNATION, DESIG, GID, CROSS, SOURCE, ENTRY_CODE, PLOT, CHECK, TYPE_OF_ENTRY, CODE, ASSIGNED, C, CONSTANT, VARIATE

	, EXPORT_STUDY_EXCEL, EXPORT_KSU_EXCEL, EXPORT_KSU_CSV, EXPORT_CSV

	, LIST_DATE, LIST_TYPE

	, FILE_NOT_EXCEL_ERROR, FILE_NOT_CSV_ERROR, FILE_NOT_FOUND_ERROR

	, EXPORT_XLS_SUFFIX, EXPORT_CSV_SUFFIX, EXPORT_KSU_TRAITS_SUFFIX

	, CREATE_STUDY_REQUIRED_FIELDS, CREATE_PLOT_REQUIRED_FIELDS, HIDE_STUDY_FIELDS, HIDE_STUDY_VARIABLE_DBCV_FIELDS, HIDE_STUDY_VARIABLE_SETTINGS_FIELDS, HIDE_PLOT_FIELDS, HIDE_GERMPLASM_DESCRIPTOR_HEADER_TABLE, FILTER_STUDY_FIELDS, ID_NAME_COMBINATION, ID_CODE_NAME_COMBINATION_STUDY, BREEDING_METHOD_ID_CODE_NAME_COMBINATION, ID_CODE_NAME_COMBINATION_VARIATE, HIDE_ID_VARIABLES, HIDE_STUDY_DETAIL_VARIABLES, CREATE_STUDY_ENVIRONMENT_REQUIRED_FIELDS, CREATE_STUDY_PLOT_REQUIRED_FIELDS, CREATE_STUDY_EXP_DESIGN_DEFAULT_FIELDS, CREATE_STUDY_REMOVE_TREATMENT_FACTOR_IDS, HIDE_STUDY_ENVIRONMENT_FIELDS, HIDE_STUDY_ENVIRONMENT_FIELDS_FROM_POPUP, DEFAULT_NO_OF_ENVIRONMENT_COUNT, CHECK_VARIABLES, STUDY_BASIC_DETAIL_FIELDS_HIDDEN_LABELS

	, CREATE_STUDY_ENTRY_DETAILS_REQUIRED_FIELDS

	, LOCATION_ID, BREEDING_METHOD_ID, BREEDING_METHOD_CODE, COOPERATOR_ID, COOPERATOR_NAME

	, LABEL

	, NUMERIC_DATA_TYPE

	, CROP_WHEAT, CROP_MAIZE

	, MAIZE_BREEDING_METHOD_SELFED_SHELLED, MAIZE_BREEDING_METHOD_SELFED_BULKED, MAIZE_BREEDING_METHOD_SIB_INCREASED, MAIZE_BREEDING_METHOD_COLCHICINIZE

	, ZIP_FILE_SUFFIX, TRIAL_INSTANCE_FACTOR, EXPERIMENTAL_DESIGN_VALUES, REPLICATES_VALUES, BLOCK_SIZE_VALUES, BLOCK_PER_REPLICATE_VALUES, VALUES, BLOCK_PER_REPLICATE, REPLICATES, BLOCK_SIZE, EXPERIMENTAL_DESIGN

	, MANNER_IN_TURN, MANNER_PER_LOCATION

	, DESIGN_LAYOUT_SAME_FOR_ALL, DESIGN_LAYOUT_INDIVIDUAL

	, OBJECTIVE_ID, OCC, STUDIES

	, FOLDER_ICON_PNG, STUDY_ICON_PNG, BASIC_DETAILS_PNG

	, HIDDEN_FIELDS, SPFLD_ENTRIES, SPFLD_COUNT_VARIATES, SPFLD_HAS_FIELDMAP, SPFLD_PLOT_COUNT, SELECTION_VARIATES_PROPERTIES, FIXED_STUDY_VARIABLES

	, PROPERTY_BREEDING_METHOD, PROPERTY_PLANTS_SELECTED

	, ADVANCING_YEAR_RANGE, CHAR_LIMIT, PLEASE_CHOOSE

	, BM_CODE, DBID, DBCV, ID_SUFFIX

	, TABLE_HEADER_KEY_SUFFIX

	, STUDY_BASIC_REQUIRED_FIELDS, EXP_DESIGN_TIME_LIMIT

	, ENTRY_TYPE_ID, HIDE_STUDY_VARIABLES, EXP_DESIGN_VARIABLES, EXP_DESIGN_REQUIRED_VARIABLES, STANDARD_VARIABLE_NAME_LIMIT, STUDY

	, MESSAGE

	, EXPORT_ADVANCE_STUDY_EXCEL, EXPORT_ADVANCE_STUDY_CSV

	, TEMPORARY_INVENTORY_SCALE, TEMPORARY_INVENTORY_COMMENT

	, ADVANCE_ZIP_DEFAULT_FILENAME

	, FILTER_MEAN_AND_STATISCAL_VARIABLES_IS_A_IDS

	, DESIGN_TEMPLATE_ALPHA_LATTICE_FOLDER, PARENT_LIST_TYPE, PARENT_LIST_DESCRIPTION;

	private static final Logger LOG = LoggerFactory.getLogger(AppConstants.class);

	private static final Properties configFile = new Properties();

	public static final String PROPERTY_FILE = "appconstants.properties";

	static {
		try {
			AppConstants.configFile.load(AppConstants.class.getClassLoader().getResourceAsStream(AppConstants.PROPERTY_FILE));
		} catch (final IOException e) {
			AppConstants.LOG.error("Error accessing property file: " + AppConstants.PROPERTY_FILE, e);
		}

	}

	public int getInt() {
		int appConstant = -1;
		final String value = this.getString().trim();
		appConstant = Integer.valueOf(value);
		return appConstant;
	}

	public boolean isInt() {
		final String value = this.getString().trim();
		try {
			Integer.valueOf(value);
		} catch (final NumberFormatException e) {
			return false;
		}
		return true;
	}

	public String getString() {
		String value = null;
		try {
			value = AppConstants.configFile.getProperty(this.toString());
		} catch (final NumberFormatException e) {
			AppConstants.LOG.error("Value not numeric.", e);
		}
		return value;
	}

	public static String getString(final String labelKey) {
		String value = null;
		try {
			value = AppConstants.configFile.getProperty(labelKey);
		} catch (final NumberFormatException e) {
			AppConstants.LOG.error("Value not numeric.", e);
		}
		return value;
	}

	public Map<String, String> getMapOfValues() {
		final String constantValue = this.getString();
		final Map<String, String> map = new HashMap<>();
		final String[] pairs = constantValue.split(",");
		for (final String pair : pairs) {
			final String[] separated = pair.split("\\|");
			if (separated.length == 2) {
				map.put(separated[0], separated[1]);
			}
		}
		return map;
	}

	public List<String> getList() {
		final String[] arr = this.getString().split(",");
		return Arrays.asList(arr);
	}

	public List<Integer> getIntegerList() {
		final List<Integer> list = new ArrayList<>();
		final String[] arr = this.getString().split(",");
		for (final String rec : arr) {
			if (NumberUtils.isNumber(rec)) {
				list.add(Integer.valueOf(rec));
			}
		}
		return list;
	}
}
