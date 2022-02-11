
package org.generationcp.commons.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.generationcp.commons.exceptions.GermplasmListExporterException;
import org.generationcp.commons.parsing.GermplasmExportedWorkbook;
import org.generationcp.commons.pojo.ExportColumnHeader;
import org.generationcp.commons.pojo.ExportRow;
import org.generationcp.commons.pojo.GermplasmListExportInputValues;
import org.generationcp.commons.service.GermplasmExportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * see {@link org.generationcp.commons.service.GermplasmExportService} documentation
 */
public class GermplasmExportServiceImpl implements GermplasmExportService {

	// create workbook
	@Resource
	private GermplasmExportedWorkbook wb;

	private static final Logger LOG = LoggerFactory.getLogger(GermplasmExportServiceImpl.class);

	/**
	 * Default constructor for spring
	 */
	public GermplasmExportServiceImpl() {

	}

	/**
	 * Test constructor
	 *
	 * @param wb mock {@link GermplasmExportedWorkbook}
	 */
	public GermplasmExportServiceImpl(final GermplasmExportedWorkbook wb) {
		this.wb = wb;
	}

	@Override
	public File generateCSVFile(final List<ExportRow> exportRows, final List<ExportColumnHeader> exportColumnHeaders,
			final String fileNameFullPath) throws IOException {
		return this.generateCSVFile(exportRows, exportColumnHeaders, fileNameFullPath, true);
	}

	@Override
	public File generateCSVFile(final List<ExportRow> exportRows, final List<ExportColumnHeader> exportColumnHeaders,
			final String fileNameFullPath, final boolean includeHeader) throws IOException {
		final File newFile = new File(fileNameFullPath);

		final CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(fileNameFullPath), "UTF-8"), ',');

		// feed in your array (or convert your data to an array)
		final List<String[]> rowValues = new ArrayList<>();
		if (includeHeader) {
			rowValues.add(this.getColumnHeaderNames(exportColumnHeaders));
		}
		for (final ExportRow row : exportRows) {
			rowValues.add(this.getColumnValues(row, exportColumnHeaders));
		}
		writer.writeAll(rowValues);
		writer.close();
		return newFile;
	}

	protected String[] getColumnValues(final ExportRow row, final List<ExportColumnHeader> exportColumnHeaders) {
		final List<String> values = new ArrayList<>();
		for (final ExportColumnHeader exportColumnHeader : exportColumnHeaders) {
			if (exportColumnHeader.isDisplay()) {
				values.add(row.getValueForColumn(exportColumnHeader.getId()));
			}
		}
		return values.toArray(new String[values.size()]);
	}

	protected String[] getColumnHeaderNames(final List<ExportColumnHeader> exportColumnHeaders) {
		final List<String> values = new ArrayList<>();
		for (final ExportColumnHeader exportColumnHeader : exportColumnHeaders) {
			if (exportColumnHeader.isDisplay()) {
				values.add(exportColumnHeader.getName());
			}
		}
		return values.toArray(new String[values.size()]);
	}

	/**
	 * Main workbook generation entry point. Uses the GermplasmExportedWorkbook class to build an Excel style workbook to export.
	 */
	@Override
	public FileOutputStream generateGermplasmListExcelFile(final GermplasmListExportInputValues input)
			throws GermplasmListExporterException {
		this.wb.init(input);
		final String filename = input.getFileName();
		try {
			// write the excel file
			final FileOutputStream fileOutputStream = new FileOutputStream(filename);
			this.wb.write(fileOutputStream);
			fileOutputStream.close();
			return fileOutputStream;
		} catch (final Exception ex) {
			GermplasmExportServiceImpl.LOG.error(ex.getMessage(), ex);
			throw new GermplasmListExporterException();
		}
	}

}
