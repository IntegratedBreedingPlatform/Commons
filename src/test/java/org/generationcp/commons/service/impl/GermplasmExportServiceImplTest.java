
package org.generationcp.commons.service.impl;

import au.com.bytecode.opencsv.CSVReader;
import junit.framework.Assert;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.generationcp.commons.exceptions.GermplasmListExporterException;
import org.generationcp.commons.parsing.GermplasmExportTestHelper;
import org.generationcp.commons.parsing.GermplasmExportedWorkbook;
import org.generationcp.commons.pojo.ExportColumnHeader;
import org.generationcp.commons.pojo.ExportRow;
import org.generationcp.commons.pojo.GermplasmListExportInputValues;
import org.generationcp.commons.service.FileService;
import org.generationcp.middleware.domain.oms.TermId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GermplasmExportServiceImplTest {

	@Mock
	private FileService fileService;

	@InjectMocks
	private final GermplasmExportServiceImpl germplasmExportService =
			new GermplasmExportServiceImpl(Mockito.mock(GermplasmExportedWorkbook.class));

	private List<ExportColumnHeader> columnsHeaders;
	private List<ExportRow> exportRows;
	private String sheetName;
	private GermplasmListExportInputValues input;

	@Before
	public void setUp() throws InvalidFormatException, IOException {
		MockitoAnnotations.initMocks(this);

		this.columnsHeaders = this.generateSampleExportColumnHeader();
		this.exportRows = this.generateSampleExportRows(10, false);
		this.sheetName = "List";

		this.input = GermplasmExportTestHelper.generateGermplasmListExportInputValues();

		Mockito.doReturn(GermplasmExportTestHelper.createWorkbook()).when(this.fileService)
				.retrieveWorkbookTemplate(GermplasmExportTestHelper.TEST_FILE_NAME);
	}

	@After
	public void tearDown() {
		final File file = new File(GermplasmExportTestHelper.TEST_FILE_NAME);
		file.deleteOnExit();
	}

	@Test
	public void testGenerateCSVFile() throws IOException {

		final File generatedFile = this.germplasmExportService.generateCSVFile(this.exportRows, this.columnsHeaders,
				GermplasmExportTestHelper.TEST_FILE_NAME);

		final CSVReader reader = new CSVReader(new FileReader(generatedFile), ',');

		int index = 0;
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			// Verifying the read data here
			final String[] actualData;
			if (index == 0) {
				// get the columns
				actualData = this.germplasmExportService.getColumnHeaderNames(this.columnsHeaders);
			} else {
				// the actual data
				actualData = this.germplasmExportService.getColumnValues(this.exportRows.get(index - 1), this.columnsHeaders);
			}
			Assert.assertEquals("Should have the same value in the file and the java representation of the string arrays",
					Arrays.toString(actualData), Arrays.toString(nextLine));
			index++;
		}
		reader.close();
	}

	@Test
	public void testGenerateCSVFileWithHeader() throws IOException {

		final File generatedFile = this.germplasmExportService.generateCSVFile(this.exportRows, this.columnsHeaders,
				GermplasmExportTestHelper.TEST_FILE_NAME, true);

		final CSVReader reader = new CSVReader(new FileReader(generatedFile), ',');

		int index = 0;
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			// Verifying the read data here
			final String[] actualData;
			if (index == 0) {
				// get the columns
				actualData = this.germplasmExportService.getColumnHeaderNames(this.columnsHeaders);
			} else {
				// the actual data
				actualData = this.germplasmExportService.getColumnValues(this.exportRows.get(index - 1), this.columnsHeaders);
			}
			Assert.assertEquals("Should have the same value in the file and the java representation of the string arrays",
					Arrays.toString(actualData), Arrays.toString(nextLine));
			index++;
		}
		reader.close();
	}

	@Test
	public void testGenerateCSVFileWithoutHeader() throws IOException {

		final File generatedFile = this.germplasmExportService.generateCSVFile(this.exportRows, this.columnsHeaders,
				GermplasmExportTestHelper.TEST_FILE_NAME, false);

		final CSVReader reader = new CSVReader(new FileReader(generatedFile), ',');

		int index = 0;
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			// Verifying the read data here
			final String[] actualData;
			// we don't inclde the header in the checking
			// the actual data
			actualData = this.germplasmExportService.getColumnValues(this.exportRows.get(index), this.columnsHeaders);

			Assert.assertEquals("Should have the same value in the file and the java representation of the string arrays",
					Arrays.toString(actualData), Arrays.toString(nextLine));
			index++;
		}
		reader.close();
	}

	@Test
	public void testGetColumnValues() {
		String actualData[];
		for (final ExportRow row : this.exportRows) {
			actualData = this.germplasmExportService.getColumnValues(row, this.columnsHeaders);
			Assert.assertEquals("Should have the same size of column values", actualData.length, this.columnsHeaders.size());

		}
	}

	@Test
	public void testGetColumnHeaderNames() {
		final String[] actualData = this.germplasmExportService.getColumnHeaderNames(this.columnsHeaders);
		Assert.assertEquals("Should have the same size of column names", actualData.length, this.columnsHeaders.size());
	}

	private List<ExportRow> generateSampleExportRows(final int rows, final boolean isSeedAmountBlank) {
		final List<ExportRow> exportRows = new ArrayList<>();
		for (int i = 0; i < rows; i++) {
			final ExportRow row = new ExportRow();
			for (int j = 0; j < this.columnsHeaders.size(); j++) {
				final ExportColumnHeader header = this.columnsHeaders.get(j);
				if (Integer.valueOf(TermId.SEED_AMOUNT_G.getId()).equals(header.getId())) {
					final String decimalString = isSeedAmountBlank ? "": i + ".0";
					row.addColumnValue(header.getId(), decimalString);
				} else {
					row.addColumnValue(header.getId(), "Row " + i + ": , Value -" + j);
				}
			}
			exportRows.add(row);
		}
		return exportRows;
	}

	private List<ExportColumnHeader> generateSampleExportColumnHeader() {
		final List<ExportColumnHeader> exportColumnHeaders = new ArrayList<>();
		exportColumnHeaders.add(new ExportColumnHeader(TermId.ENTRY_NO.getId(), TermId.ENTRY_NO.toString(), true));
		exportColumnHeaders.add(new ExportColumnHeader(TermId.DESIG.getId(), TermId.DESIG.toString(), true));
		exportColumnHeaders.add(new ExportColumnHeader(TermId.CROSS.getId(), TermId.CROSS.toString(), true));
		exportColumnHeaders.add(new ExportColumnHeader(TermId.GID.getId(), TermId.GID.toString(), true));
		exportColumnHeaders.add(new ExportColumnHeader(TermId.SOURCE.getId(), TermId.SOURCE.toString(), true));
		exportColumnHeaders.add(new ExportColumnHeader(TermId.DUPLICATE.getId(), TermId.DUPLICATE.toString(), true));
		exportColumnHeaders.add(new ExportColumnHeader(TermId.BULK_WITH.getId(), TermId.BULK_WITH.toString(), true));
		exportColumnHeaders.add(new ExportColumnHeader(TermId.BULK_COMPL.getId(), TermId.BULK_COMPL.toString(), true));
		exportColumnHeaders.add(new ExportColumnHeader(TermId.LOCATION_ABBR.getId(), TermId.LOCATION_ABBR.toString(), true));
		exportColumnHeaders.add(new ExportColumnHeader(TermId.SEED_AMOUNT_G.getId(), TermId.SEED_AMOUNT_G.toString(), true));
		exportColumnHeaders.add(new ExportColumnHeader(TermId.COMMENT_INVENTORY.getId(), TermId.COMMENT_INVENTORY.toString(), true));
		return exportColumnHeaders;
	}

	@Test
	public void testGenerateGermplasmListExcelFile() throws GermplasmListExporterException {
		this.germplasmExportService.generateGermplasmListExcelFile(this.input);
	}

}
