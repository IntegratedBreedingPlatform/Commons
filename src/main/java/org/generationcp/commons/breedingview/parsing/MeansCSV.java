
package org.generationcp.commons.breedingview.parsing;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.commons.exceptions.BreedingViewInvalidFormatException;

import au.com.bytecode.opencsv.CSVReader;
import org.generationcp.middleware.domain.oms.TermId;

/**
 * This class parses a file and creates a map of variable names to a list of trait and means values
 *
 */
public class MeansCSV {

	public static final String UNIT_ERRORS_SUFFIX = "_UnitErrors";
	public static final String MEANS_SUFFIX = "_Means";
	protected static final String FORMAT_IS_INVALID_FOR_MEANS_DATA = "Cannot parse the file because the format is invalid for MEANS data.";
	private final Map<String, String> nameToAliasMapping;
	private final File file;
	public MeansCSV(final File file, final Map<String, String> nameToAliasMapping) {
		this.file = file;
		this.nameToAliasMapping = nameToAliasMapping;
	}

	/**
	 * Parse csv file and return a map of column header names with their corresponding list of values
	 *
	 * @return
	 * @throws IOException
	 */
	public Map<String, List<String>> getData() throws IOException {

		final CSVReader reader = new CSVReader(new FileReader(this.file));
		final Map<String, List<String>> csvMap = new LinkedHashMap<>();
		final String[] headers = reader.readNext();
		// track columns to skip - they are skipped because they are duplicates and/or for Unit Errors variables
		final List<Integer> columnIndexesToSkip = new ArrayList<>();
		int columnIndex = 0;
		for (final String headerCol : headers) {
			// Trim the suffixes from BV output file to extract trait name
			final String aliasLocalName = headerCol.trim().replace(MeansCSV.MEANS_SUFFIX, "").replace(MeansCSV.UNIT_ERRORS_SUFFIX, "");
			String actualLocalName = null;

			actualLocalName = this.nameToAliasMapping.get(aliasLocalName);
			if (actualLocalName == null) {
				actualLocalName = aliasLocalName;
			}

			final String newHeaderName = headerCol.trim().replace(aliasLocalName, actualLocalName);
			// Mark duplicate columns for skipping when building map
			if (csvMap.containsKey(newHeaderName)) {
				columnIndexesToSkip.add(columnIndex);

				// Exclude "Unit Errors" analysis variables from map as we won't be saving them
			} else if (newHeaderName.endsWith(MeansCSV.UNIT_ERRORS_SUFFIX)) {
				columnIndexesToSkip.add(columnIndex);

				//Exclude the additional column, happens when the environment factor is not TRIAL_INSTANCE
			} else if (TermId.ENTRY_NO.name().equals(actualLocalName) && columnIndex == 2) {
				columnIndexesToSkip.add(1);
				csvMap.put(newHeaderName, new ArrayList<String>());
				csvMap.remove(headers[1]);
			} else {
				csvMap.put(newHeaderName, new ArrayList<String>());
			}
			columnIndex++;

		}
		// Add the cell values to list of values per column header
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			for (int i = 0; i < headers.length; i++) {
				if (!columnIndexesToSkip.contains(i)) {
					final String headerName = headers[i];
					csvMap.get(headerName).add(nextLine[i].trim());
				}
			}
		}

		reader.close();

		return csvMap;
	}

	public void validate() throws BreedingViewInvalidFormatException {

		int meansCounter = 0;

		CSVReader reader;
		String[] header = new String[] {};

		try {
			reader = new CSVReader(new FileReader(this.file));
			header = reader.readNext();
			reader.close();
		} catch (final Exception e) {
			throw new BreedingViewInvalidFormatException("A problem occurred while reading the MEANS data file", e);
		}

		for (final String s : header) {
			if (s.contains(MeansCSV.MEANS_SUFFIX)) {
				meansCounter++;
			}
		}

		if (meansCounter == 0) {
			throw new BreedingViewInvalidFormatException(MeansCSV.FORMAT_IS_INVALID_FOR_MEANS_DATA);
		}
	}

}
