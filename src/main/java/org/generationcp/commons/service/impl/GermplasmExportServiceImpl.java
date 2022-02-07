
package org.generationcp.commons.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.commons.exceptions.GermplasmListExporterException;
import org.generationcp.commons.parsing.GermplasmExportedWorkbook;
import org.generationcp.commons.pojo.ExportColumnHeader;
import org.generationcp.commons.pojo.ExportRow;
import org.generationcp.commons.pojo.GermplasmListExportInputValues;
import org.generationcp.commons.service.GermplasmExportService;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * see {@link org.generationcp.commons.service.GermplasmExportService} documentation
 */
public class GermplasmExportServiceImpl implements GermplasmExportService {

	// create workbook
	@Resource
	private GermplasmExportedWorkbook wb;

	private static final Logger LOG = LoggerFactory.getLogger(GermplasmExportServiceImpl.class);

	protected static final List<Integer> NUMERIC_IDS = Lists.newArrayList(TermId.ENTRY_NO.getId(), TermId.GID.getId());

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

	protected HSSFWorkbook createWorkbookForSingleSheet(final List<ExportRow> exportRows,
			final List<ExportColumnHeader> exportColumnHeaders, final String sheetName) {
		final HSSFWorkbook wb = new HSSFWorkbook();
		final HSSFSheet sheet = wb.createSheet(sheetName);

		int rowIndex = 0;
		this.writeColumHeaders(exportColumnHeaders, wb, sheet, rowIndex);
		rowIndex++;

		rowIndex = this.writeColumnValues(exportColumnHeaders, exportRows, sheet, rowIndex, wb);

		for (int ctr = 0; ctr < rowIndex; ctr++) {
			sheet.autoSizeColumn(rowIndex);
		}
		return wb;
	}

	protected int writeColumnValues(final List<ExportColumnHeader> exportColumnHeaders, final List<ExportRow> exportRows,
			final HSSFSheet sheet, final int rowIndex, final Workbook workbook) {
		// Initialize once - cell style with values formatted as decimal
		final CellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("0.0"));

		int currentRowIndex = rowIndex;
		for (final ExportRow exportRow : exportRows) {
			final HSSFRow row = sheet.createRow(currentRowIndex);

			int columnIndex = 0;
			for (final ExportColumnHeader columnHeader : exportColumnHeaders) {
				final Integer id = columnHeader.getId();
				final String value = exportRow.getValueForColumn(id);
				final HSSFCell cell = row.createCell(columnIndex);
				// Cannot check data type at this point without additional Middleware query,
				// Format inventory amount as numeric cell type and cast GID, ENTRY_NO as numeric values
				if (Integer.valueOf(TermId.SEED_AMOUNT_G.getId()).equals(id) && !StringUtil.isEmpty(value)) {
					cell.setCellType(Cell.CELL_TYPE_NUMERIC);
					cell.setCellStyle(cellStyle);
					cell.setCellValue(Double.valueOf(value));
				} else if (GermplasmExportServiceImpl.NUMERIC_IDS.contains(id) && NumberUtils.isDigits(value)) {
					cell.setCellValue(Integer.parseInt(value));
				} else {
					cell.setCellValue(value);
				}
				columnIndex++;
			}
			currentRowIndex++;

		}
		return currentRowIndex;
	}

	protected void writeColumHeaders(final List<ExportColumnHeader> exportColumnHeaders, final HSSFWorkbook xlsBook, final HSSFSheet sheet,
			final int rowIndex) {
		final int noOfColumns = exportColumnHeaders.size();
		final HSSFRow header = sheet.createRow(rowIndex);
		final CellStyle greenBg = this.getHeaderStyle(xlsBook, 51, 153, 102);
		final CellStyle blueBg = this.getHeaderStyle(xlsBook, 51, 51, 153);
		for (int i = 0; i < noOfColumns; i++) {
			final ExportColumnHeader columnHeader = exportColumnHeaders.get(i);
			final Cell cell = header.createCell(i);
			if (columnHeader.getHeaderColor() != null) {
				if (columnHeader.getHeaderColor().intValue() == ExportColumnHeader.GREEN.intValue()) {
					cell.setCellStyle(greenBg);
				} else if (columnHeader.getHeaderColor().intValue() == ExportColumnHeader.BLUE.intValue()) {
					cell.setCellStyle(blueBg);
				}
			}
			cell.setCellValue(columnHeader.getName().toUpperCase());
		}
	}

	private CellStyle getHeaderStyle(final HSSFWorkbook xlsBook, final int c1, final int c2, final int c3) {
		final HSSFPalette palette = xlsBook.getCustomPalette();
		final HSSFColor color = palette.findSimilarColor(c1, c2, c3);
		final short colorIndex = color.getIndex();

		final HSSFFont whiteFont = xlsBook.createFont();
		whiteFont.setColor(new HSSFColor.WHITE().getIndex());

		final CellStyle cellStyle = xlsBook.createCellStyle();
		cellStyle.setFillForegroundColor(colorIndex);
		cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		cellStyle.setFont(whiteFont);

		return cellStyle;
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
