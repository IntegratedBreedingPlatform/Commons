package org.generationcp.commons.breedingview.parsing;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.generationcp.commons.exceptions.BreedingViewInvalidFormatException;

import au.com.bytecode.opencsv.CSVReader;

/**
 * This class parses a file and creates a map of variable names to a list of summary statistics values
 *
 */
public class SummaryStatsCSV {

	protected static final String FORMAT_IS_INVALID_FOR_SUMMARY_STATISTICS =
			"Cannot parse the file because the format is invalid for Summary Statistics.";
	protected static final String[] SUMMARY_STATS_METHODS = {"MEAN", "MEANSED", "PVALUE", "HERITABILITY"};
	private static final int ENVIRONMENT_INDEX = 0;
	private static final int TRAIT_INDEX = 1;
	private static final int SUMMARY_METHODS_START_INDEX = 2;
	private final File file;
	private Map<String, Map<String, ArrayList<String>>> data;
	private final Map<String, String> nameToAliasMapping;
	private String[] header;

	public SummaryStatsCSV(final File file, final Map<String, String> nameToAliasMapping) {
		this.file = file;
		this.nameToAliasMapping = nameToAliasMapping;
	}

	public List<String> getHeaders() throws IOException {

		this.data = this.getData();

		return Arrays.asList(this.header);
	}

	/**
	 * Return list of column headers for summary statistic methods
	 * 
	 * @return
	 * @throws IOException
	 */
	public List<String> getSummaryHeaders() throws IOException {

		this.data = this.getData();

		final List<String> list = new ArrayList<String>(Arrays.asList(this.header));
		// Remove the first column header(s) which are the environment and trait headers
		for (int i=0; i<SUMMARY_METHODS_START_INDEX; i++) {
			list.remove(0);
		}
		return list;
	}

	public String getTrialHeader() throws IOException {
		final String trialHeader = this.getHeaders().get(ENVIRONMENT_INDEX);
		String actualLocalName = this.nameToAliasMapping.get(trialHeader);
		if (actualLocalName == null) {
			actualLocalName = trialHeader;
		}
		return actualLocalName;
	}

	/**
	 * Return map of environment names with corresponding map of trait names to list of summary values
	 * for saving. Only include values for summary columns: Mean, MeanSed, Heritability and PValue, other
	 * summary columns will not be included.
	 * 
	 * @return
	 * @throws IOException
	 */
	public Map<String, Map<String, ArrayList<String>>> getData() throws IOException {
		if (this.data != null) {
			return this.data;
		}
		final CSVReader reader = new CSVReader(new FileReader(this.file));
		this.data = new LinkedHashMap<String, Map<String, ArrayList<String>>>();
		
		// Track columns to skip - we will not save all summary columns in BMSSummary.csv
		final List<Integer> columnIndicesToSkip = new ArrayList<>();
		final String[] originalHeaders = reader.readNext();
		final List<String> finalHeaders = new ArrayList<>();
		
		// Include columns for environment factor and "Trait"
		finalHeaders.add(originalHeaders[ENVIRONMENT_INDEX].toUpperCase());
		finalHeaders.add(originalHeaders[TRAIT_INDEX].toUpperCase());
		// Filter to the summary methods we are interested in
		for (int i=SUMMARY_METHODS_START_INDEX; i < originalHeaders.length; i++){
			final String originalHeader = originalHeaders[i];
			final String capitalizedHeader = originalHeader.toUpperCase();
			if (!ArrayUtils.contains(SUMMARY_STATS_METHODS, capitalizedHeader)){
				columnIndicesToSkip.add(i);
			} else {
				finalHeaders.add(originalHeader);
			}
		}
		this.header = finalHeaders.toArray(new String[0]);
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			final String environment = nextLine[0].trim();
			String trait = null;

			final String traitString = this.nameToAliasMapping.get(nextLine[1]);
			if (traitString != null) {
				trait = traitString.trim();
			}

			if (trait == null) {
				trait = nextLine[1].trim();
			}

			if (!this.data.containsKey(environment)) {
				this.data.put(environment, new LinkedHashMap<String, ArrayList<String>>());
			}

			if (!this.data.get(environment).containsKey(trait)) {
				this.data.get(environment).put(trait, new ArrayList<String>());
			}
			for (int i = SUMMARY_METHODS_START_INDEX; i < originalHeaders.length; i++) {
				if (!columnIndicesToSkip.contains(i)){
					this.data.get(environment).get(trait).add(nextLine[i].trim());
				}
			}
		}

		reader.close();
		return this.data;
	}

	public void validate() throws BreedingViewInvalidFormatException {

		CSVReader reader;
		String[] fileHeaders = new String[] {};

		try {
			reader = new CSVReader(new FileReader(this.file));
			fileHeaders = reader.readNext();
			reader.close();
		} catch (final Exception e) {
			throw new BreedingViewInvalidFormatException("A problem occurred while reading the Summary Statistics data file", e);
		}

		final List<String> headerList = Arrays.asList(fileHeaders);

		if (!headerList.containsAll(Arrays
				.asList("Trait,NumValues,NumMissing,Mean,Variance,SD,Min,Max,Range,Median,LowerQuartile,UpperQuartile,MeanRep,MinRep,MaxRep,MeanSED,MinSED,MaxSED,MeanLSD,MinLSD,MaxLSD,CV,Heritability,WaldStatistic,WaldDF,Pvalue"
						.split(",")))) {
			throw new BreedingViewInvalidFormatException(FORMAT_IS_INVALID_FOR_SUMMARY_STATISTICS);
		}
	}

}
