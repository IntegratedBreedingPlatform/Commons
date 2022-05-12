
package org.generationcp.commons.parsing;

import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.generationcp.commons.exceptions.GermplasmListExporterException;
import org.generationcp.commons.pojo.GermplasmListExportInputValues;
import org.generationcp.commons.workbook.generator.CodesSheetGenerator;
import org.generationcp.commons.workbook.generator.GermplasmAttributesWorkbookExporter;
import org.generationcp.commons.workbook.generator.GermplasmNamesWorkbookExporter;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.interfaces.GermplasmExportSource;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;

import junit.framework.Assert;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
@RunWith(MockitoJUnitRunner.class)
public class GermplasmExportedWorkbookTest {

	private final GermplasmListExportInputValues input = GermplasmExportTestHelper.generateGermplasmListExportInputValues();

	@Mock
	private CodesSheetGenerator codesSheetGenerator;

	@Mock
	private GermplasmAttributesWorkbookExporter attributesGenerator;

	@Mock
	private GermplasmNamesWorkbookExporter namesGenerator;

	@InjectMocks
	private GermplasmExportedWorkbook germplasmExportedWorkbook;

	@Before
	public void setUp() {
		Mockito.when(this.namesGenerator.addRowsToDescriptionSheet(Matchers.any(HSSFSheet.class), Matchers.anyInt(), Matchers.any(ExcelCellStyleBuilder.class), Matchers.eq(this.input.getCurrentColumnsInfo()))). thenReturn(18);
		Mockito.when(this.namesGenerator.generateAddedColumnHeader(Matchers.any(HSSFRow.class), Matchers.anyInt())).thenReturn(7);
		Mockito.when(this.namesGenerator.generateAddedColumnValue(Matchers.any(HSSFRow.class), Matchers.any(GermplasmExportSource.class), Matchers.anyInt())).thenReturn(7);
	}
	@Test
	public void testGetNoOfVisibleColumns() {

		Assert.assertTrue("Expected that the number of visibleColums = " + this.input.getVisibleColumnMap().size(),
				this.germplasmExportedWorkbook.getNoOfVisibleColumns(this.input.getVisibleColumnMap()) == this.input.getVisibleColumnMap()
						.size());
		final int visibleColumns = this.germplasmExportedWorkbook.getNoOfVisibleColumns(this.input.getVisibleColumnMap());
		Assert.assertTrue("Expected that the number of visibleColums = " + (this.input.getVisibleColumnMap().size() - 1),
				visibleColumns == this.input.getVisibleColumnMap().size() - 1);
	}

	@Test
	public void testGenerateObservationSheet() throws GermplasmListExporterException {
		// input data
		final HSSFWorkbook wb = GermplasmExportTestHelper.createWorkbook();
		final GermplasmList germplasmList = this.input.getGermplasmList();
		final List<GermplasmListData> listDatas = germplasmList.getListData();
		final ExcelCellStyleBuilder styles = new ExcelCellStyleBuilder(wb);

		this.germplasmExportedWorkbook.init(this.input);
		final HSSFSheet observationSheet = this.germplasmExportedWorkbook.getWorkbook().getSheet("Observation");

		final Map<String, Boolean> visibleColumnMap = this.input.getVisibleColumnMap();
		// Assert Header Row
		HSSFRow row = observationSheet.getRow(0);
		int columnIndex = 0;
		if (visibleColumnMap.get(String.valueOf(TermId.ENTRY_NO.getId()))) {
			Assert.assertEquals("Expecting correct header for " + TermId.ENTRY_NO.toString(), TermId.ENTRY_NO.toString(),
					row.getCell(columnIndex).getStringCellValue());
			columnIndex++;
		}
		if (visibleColumnMap.get(String.valueOf(TermId.GID.getId()))) {
			Assert.assertEquals("Expecting correct header for " + TermId.GID.toString(), TermId.GID.toString(),
					row.getCell(columnIndex).getStringCellValue());
			columnIndex++;
		}
		if (visibleColumnMap.get(String.valueOf(TermId.ENTRY_CODE.getId()))) {
			Assert.assertEquals("Expecting correct header for " + TermId.ENTRY_CODE.toString(), TermId.ENTRY_CODE.toString(),
					row.getCell(columnIndex).getStringCellValue());
			columnIndex++;
		}
		if (visibleColumnMap.get(String.valueOf(TermId.DESIG.getId()))) {
			Assert.assertEquals("Expecting correct header for DESIGNATION.", "DESIGNATION", row.getCell(columnIndex).getStringCellValue());
			columnIndex++;
		}
		if (visibleColumnMap.get(String.valueOf(TermId.CROSS.getId()))) {
			Assert.assertEquals("Expecting correct header for " + TermId.CROSS.toString(), TermId.CROSS.toString(),
					row.getCell(columnIndex).getStringCellValue());
			columnIndex++;
		}

		if (visibleColumnMap.get(String.valueOf(TermId.GROUPGID.getId()))) {
			Assert.assertEquals("Expecting correct header for " + TermId.GROUPGID.toString(), TermId.GROUPGID.toString(),
					row.getCell(columnIndex).getStringCellValue());
			columnIndex++;
		}

		Assert.assertEquals("Expecting correct header for STOCKID", "STOCKID",
			row.getCell(columnIndex).getStringCellValue());
		columnIndex++;
		// Assert Row Values
		int rowIndex = 1;
		for (final GermplasmListData listData : listDatas) {
			row = observationSheet.getRow(rowIndex);

			columnIndex = 0;
			if (visibleColumnMap.get(String.valueOf(TermId.ENTRY_NO.getId()))) {
				Assert.assertEquals("Expecting correct value for " + TermId.ENTRY_NO.toString() + " at Row " + (rowIndex + 1),
						listData.getEntryId().doubleValue(), row.getCell(columnIndex).getNumericCellValue());
				Assert.assertEquals(styles.getCellStyle(ExcelCellStyleBuilder.ExcelCellStyle.NUMBER_COLUMN_HIGHLIGHT_STYLE_FACTOR),
						row.getCell(columnIndex).getCellStyle());
				columnIndex++;
			}
			if (visibleColumnMap.get(String.valueOf(TermId.GID.getId()))) {
				Assert.assertEquals("Expecting correct value for " + TermId.GID.toString() + " at Row " + (rowIndex + 1),
						listData.getGid().doubleValue(), row.getCell(columnIndex).getNumericCellValue());
				Assert.assertEquals(styles.getCellStyle(ExcelCellStyleBuilder.ExcelCellStyle.NUMBER_DATA_FORMAT_STYLE),
						row.getCell(columnIndex).getCellStyle());
				columnIndex++;
			}
			if (visibleColumnMap.get(String.valueOf(TermId.ENTRY_CODE.getId()))) {
				Assert.assertEquals("Expecting correct value for " + TermId.ENTRY_NO.toString() + " at Row " + (rowIndex + 1),
						listData.getEntryCode(), row.getCell(columnIndex).getStringCellValue());
				columnIndex++;
			}
			if (visibleColumnMap.get(String.valueOf(TermId.DESIG.getId()))) {
				Assert.assertEquals("Expecting correct value for DESIGNATION at Row " + (rowIndex + 1), listData.getDesignation(),
						row.getCell(columnIndex).getStringCellValue());
				columnIndex++;
			}
			if (visibleColumnMap.get(String.valueOf(TermId.CROSS.getId()))) {
				Assert.assertEquals("Expecting correct value for " + TermId.CROSS.toString() + " at Row " + (rowIndex + 1),
						listData.getGroupName(), row.getCell(columnIndex).getStringCellValue());
				columnIndex++;
			}
			if (visibleColumnMap.get(String.valueOf(TermId.GROUPGID.getId()))) {
				Assert.assertEquals("Expecting correct value for " + TermId.GROUPGID.toString() + " at Row " + (rowIndex + 1),
						listData.getGroupId().doubleValue(), row.getCell(columnIndex).getNumericCellValue());
				columnIndex++;
			}

			Assert.assertEquals("Expecting correct value for " + TermId.SEED_AMOUNT_G.toString() + " at Row " + (rowIndex + 1),
					listData.getStockIDs(), row.getCell(columnIndex).getStringCellValue());
			rowIndex++;
		}
	}

	@Test
	public void testGenerateDescriptionSheet() throws GermplasmListExporterException {
		// input data
		final GermplasmList germplasmList = this.input.getGermplasmList();
		final Map<String, Boolean> visibleColumnMap = this.input.getVisibleColumnMap();

		final Map<Integer, Term> columnTerms = this.input.getColumnTermMap();
		final Map<Integer, Variable> inventoryVariables = this.input.getInventoryVariableMap();

		this.germplasmExportedWorkbook.init(this.input);
		final HSSFSheet descriptionSheet = this.germplasmExportedWorkbook.getWorkbook().getSheet("Description");

		Assert.assertNotNull("Expected to successfully generated the description sheet.", descriptionSheet);
		Assert.assertTrue("The sheet name is Description.", "Description".equalsIgnoreCase(descriptionSheet.getSheetName()));

		HSSFRow row;
		// Assert List Details Section
		row = descriptionSheet.getRow(0);
		Assert.assertEquals("Expecting " + row.getCell(0).getStringCellValue() + " equals to LIST NAME but didn't.",
				row.getCell(0).getStringCellValue(), "LIST NAME");
		Assert.assertEquals(row.getCell(1).getStringCellValue(), germplasmList.getName());

		row = descriptionSheet.getRow(1);
		Assert.assertEquals("Expecting " + row.getCell(0).getStringCellValue() + " equals to LIST DESCRIPTION but didn't.",
				row.getCell(0).getStringCellValue(), "LIST DESCRIPTION");
		Assert.assertEquals(row.getCell(1).getStringCellValue(), germplasmList.getDescription());

		row = descriptionSheet.getRow(2);
		Assert.assertEquals("Expecting " + row.getCell(0).getStringCellValue() + " equals to LIST TYPE but didn't.",
				row.getCell(0).getStringCellValue(), "LIST TYPE");
		Assert.assertEquals(row.getCell(1).getStringCellValue(), germplasmList.getType());

		row = descriptionSheet.getRow(3);
		Assert.assertEquals("Expecting " + row.getCell(0).getStringCellValue() + " equals to LIST DATE but didn't.",
				row.getCell(0).getStringCellValue(), "LIST DATE");
		Assert.assertEquals(row.getCell(1).getStringCellValue(), germplasmList.getDate().toString());

		// Assert List Condition Section
		row = descriptionSheet.getRow(5);
		Assert.assertEquals("Expecting " + row.getCell(0).getStringCellValue() + " equals to CONDITION but didn't.",
				row.getCell(0).getStringCellValue(), "CONDITION");
		Assert.assertEquals("Expecting " + row.getCell(1).getStringCellValue() + " equals to DESCRIPTION but didn't.",
				row.getCell(1).getStringCellValue(), "DESCRIPTION");
		Assert.assertEquals("Expecting " + row.getCell(2).getStringCellValue() + " equals to PROPERTY but didn't.",
				row.getCell(2).getStringCellValue(), "PROPERTY");
		Assert.assertEquals("Expecting " + row.getCell(3).getStringCellValue() + " equals to SCALE but didn't.",
				row.getCell(3).getStringCellValue(), "SCALE");
		Assert.assertEquals("Expecting " + row.getCell(4).getStringCellValue() + " equals to METHOD but didn't.",
				row.getCell(4).getStringCellValue(), "METHOD");
		Assert.assertEquals("Expecting " + row.getCell(5).getStringCellValue() + " equals to DATA TYPE but didn't.",
				row.getCell(5).getStringCellValue(), "DATA TYPE");
		Assert.assertEquals("Expecting " + row.getCell(6).getStringCellValue() + " equals to VALUE but didn't.",
				row.getCell(6).getStringCellValue(), "VALUE");

		row = descriptionSheet.getRow(6);
		Assert.assertEquals("Expecting " + row.getCell(0).getStringCellValue() + " equals to LIST USER but didn't.",
				row.getCell(0).getStringCellValue(), "LIST USER");
		Assert.assertEquals("Expecting " + row.getCell(1).getStringCellValue() + " equals to PERSON WHO MADE THE LIST but didn't.",
				row.getCell(1).getStringCellValue(), "PERSON WHO MADE THE LIST");
		Assert.assertEquals("Expecting " + row.getCell(2).getStringCellValue() + " equals to PERSON but didn't.",
				row.getCell(2).getStringCellValue(), "PERSON");
		Assert.assertEquals("Expecting " + row.getCell(3).getStringCellValue() + " equals to DBCV but didn't.",
				row.getCell(3).getStringCellValue(), "DBCV");
		Assert.assertEquals("Expecting " + row.getCell(4).getStringCellValue() + " equals to ASSIGNED but didn't.",
				row.getCell(4).getStringCellValue(), "ASSIGNED");
		Assert.assertEquals("Expecting " + row.getCell(5).getStringCellValue() + " equals to C but didn't.",
				row.getCell(5).getStringCellValue(), "C");
		Assert.assertEquals("Expecting " + row.getCell(6).getStringCellValue() + " equals to " + this.input.getOwnerName() + " but didn't.",
				row.getCell(6).getStringCellValue(), this.input.getOwnerName());

		row = descriptionSheet.getRow(7);
		Assert.assertEquals("Expecting " + row.getCell(0).getStringCellValue() + " equals to LIST USER ID but didn't.",
				row.getCell(0).getStringCellValue(), "LIST USER ID");
		Assert.assertEquals("Expecting " + row.getCell(1).getStringCellValue() + " equals to ID OF LIST OWNER but didn't.",
				row.getCell(1).getStringCellValue(), "ID OF LIST OWNER");
		Assert.assertEquals("Expecting " + row.getCell(2).getStringCellValue() + " equals to PERSON but didn't.",
				row.getCell(2).getStringCellValue(), "PERSON");
		Assert.assertEquals("Expecting " + row.getCell(3).getStringCellValue() + " equals to DBID but didn't.",
				row.getCell(3).getStringCellValue(), "DBID");
		Assert.assertEquals("Expecting " + row.getCell(4).getStringCellValue() + " equals to ASSIGNED but didn't.",
				row.getCell(4).getStringCellValue(), "ASSIGNED");
		Assert.assertEquals("Expecting " + row.getCell(5).getStringCellValue() + " equals to N but didn't.",
				row.getCell(5).getStringCellValue(), "N");
		Assert.assertTrue("Expecting " + row.getCell(6).getNumericCellValue() + " equals to " + germplasmList.getUserId() + " but didn't.",
				row.getCell(6).getNumericCellValue() == germplasmList.getUserId());

		row = descriptionSheet.getRow(8);
		Assert.assertEquals("Expecting " + row.getCell(0).getStringCellValue() + " equals to LIST EXPORTER but didn't.",
				row.getCell(0).getStringCellValue(), "LIST EXPORTER");
		Assert.assertEquals("Expecting " + row.getCell(1).getStringCellValue() + " equals to PERSON EXPORTING THE LIST but didn't.",
				row.getCell(1).getStringCellValue(), "PERSON EXPORTING THE LIST");
		Assert.assertEquals("Expecting " + row.getCell(2).getStringCellValue() + " equals to PERSON but didn't.",
				row.getCell(2).getStringCellValue(), "PERSON");
		Assert.assertEquals("Expecting " + row.getCell(3).getStringCellValue() + " equals to DBCV but didn't.",
				row.getCell(3).getStringCellValue(), "DBCV");
		Assert.assertEquals("Expecting " + row.getCell(4).getStringCellValue() + " equals to ASSIGNED but didn't.",
				row.getCell(4).getStringCellValue(), "ASSIGNED");
		Assert.assertEquals("Expecting " + row.getCell(5).getStringCellValue() + " equals to C but didn't.",
				row.getCell(5).getStringCellValue(), "C");
		Assert.assertEquals(
				"Expecting " + row.getCell(6).getStringCellValue() + " equals to " + this.input.getExporterName() + " but didn't.",
				row.getCell(6).getStringCellValue(), this.input.getExporterName());

		row = descriptionSheet.getRow(9);
		Assert.assertEquals("Expecting " + row.getCell(0).getStringCellValue() + " equals to LIST EXPORTER ID but didn't.",
				row.getCell(0).getStringCellValue(), "LIST EXPORTER ID");
		Assert.assertEquals("Expecting " + row.getCell(1).getStringCellValue() + " equals to ID OF LIST EXPORTER but didn't.",
				row.getCell(1).getStringCellValue(), "ID OF LIST EXPORTER");
		Assert.assertEquals("Expecting " + row.getCell(2).getStringCellValue() + " equals to PERSON but didn't.",
				row.getCell(2).getStringCellValue(), "PERSON");
		Assert.assertEquals("Expecting " + row.getCell(3).getStringCellValue() + " equals to DBID but didn't.",
				row.getCell(3).getStringCellValue(), "DBID");
		Assert.assertEquals("Expecting " + row.getCell(4).getStringCellValue() + " equals to ASSIGNED but didn't.",
				row.getCell(4).getStringCellValue(), "ASSIGNED");
		Assert.assertEquals("Expecting " + row.getCell(5).getStringCellValue() + " equals to N but didn't.",
				row.getCell(5).getStringCellValue(), "N");
		Assert.assertTrue("Expecting " + row.getCell(6).getNumericCellValue() + " equals to " + this.input.getCurrentLocalIbdbUserId()
				+ "R but didn't.", row.getCell(6).getNumericCellValue() == this.input.getCurrentLocalIbdbUserId());

		// Assert List Factor Section
		row = descriptionSheet.getRow(11);
		Assert.assertEquals("Expecting " + row.getCell(0).getStringCellValue() + " equals to FACTOR but didn't.",
				row.getCell(0).getStringCellValue(), "FACTOR");
		Assert.assertEquals("Expecting " + row.getCell(1).getStringCellValue() + " equals to DESCRIPTION but didn't.",
				row.getCell(1).getStringCellValue(), "DESCRIPTION");
		Assert.assertEquals("Expecting " + row.getCell(2).getStringCellValue() + " equals to PROPERTY but didn't.",
				row.getCell(2).getStringCellValue(), "PROPERTY");
		Assert.assertEquals("Expecting " + row.getCell(3).getStringCellValue() + " equals to SCALE but didn't.",
				row.getCell(3).getStringCellValue(), "SCALE");
		Assert.assertEquals("Expecting " + row.getCell(4).getStringCellValue() + " equals to METHOD but didn't.",
				row.getCell(4).getStringCellValue(), "METHOD");
		Assert.assertEquals("Expecting " + row.getCell(5).getStringCellValue() + " equals to DATA TYPE but didn't.",
				row.getCell(5).getStringCellValue(), "DATA TYPE");
		Assert.assertEquals("Expecting " + row.getCell(6).getStringCellValue() + " equals to blank but didn't.",
				row.getCell(6).getStringCellValue(), "");

		int rowIndex = 11;

		for (final Map.Entry<String, Boolean> entry : visibleColumnMap.entrySet()) {

			if (entry.getValue()) {
				final Term term = columnTerms.get(Integer.valueOf(entry.getKey()));

				row = descriptionSheet.getRow(++rowIndex);
				Assert.assertEquals("Expecting " + row.getCell(0).getStringCellValue(), row.getCell(0).getStringCellValue(),
						term.getName().toUpperCase());
				Assert.assertEquals("Expecting " + row.getCell(1).getStringCellValue(), row.getCell(1).getStringCellValue(),
						term.getDefinition());
			}

		}

		rowIndex = rowIndex + 2;
	}

}
