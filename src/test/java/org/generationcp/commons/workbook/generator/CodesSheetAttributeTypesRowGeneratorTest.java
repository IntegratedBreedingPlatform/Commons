
package org.generationcp.commons.workbook.generator;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.generationcp.commons.data.initializer.UserDefinedFieldTestDataInitializer;
import org.generationcp.commons.parsing.ExcelCellStyleBuilder;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
@RunWith(MockitoJUnitRunner.class)
public class CodesSheetAttributeTypesRowGeneratorTest {

	private static final String NOTES = "NOTES";
	private static final String NOTE = "NOTE";
	private static final String CROP_NAME = "maize";

	private final HSSFWorkbook wb = new HSSFWorkbook();
	private final HSSFSheet codesSheet = this.wb.createSheet("Codes");
	private final ExcelCellStyleBuilder sheetStyles = new ExcelCellStyleBuilder(this.wb);

	@Mock
	GermplasmDataManager germplasmDataManager;

	@InjectMocks
	CodesSheetAttributeTypesRowGenerator attributeTypesRowGenerator;

	@Before
	public void setUp() {
		Mockito.when(this.germplasmDataManager.getUserDefinedFieldByFieldTableNameAndType(Matchers.anyString(), Matchers.anyString()))
				.thenReturn(UserDefinedFieldTestDataInitializer.createUserDefinedFieldList(CodesSheetAttributeTypesRowGeneratorTest.NOTE,
						CodesSheetAttributeTypesRowGeneratorTest.NOTES));
	}

	@Test
	public void testNameTypesRowsToCodesSheet() {
		this.attributeTypesRowGenerator.addRowsToCodesSheet(this.codesSheet, this.sheetStyles, CROP_NAME);
		final HSSFRow row = this.codesSheet.getRow(1);
		Assert.assertEquals("First cell's content should be " + RowColumnType.ATTRIBUTE_TYPES.getSection(),
				RowColumnType.ATTRIBUTE_TYPES.getSection(), row.getCell(0).toString());
		Assert.assertEquals("Second cell's content should be " + RowColumnType.ATTRIBUTE_TYPES.toString(),
				RowColumnType.ATTRIBUTE_TYPES.toString(), row.getCell(1).toString());
		Assert.assertEquals("Third cell's content should be NOTE", CodesSheetAttributeTypesRowGeneratorTest.NOTE, row.getCell(2).toString());
		Assert.assertEquals("Fourth cell's content should be NOTES", CodesSheetAttributeTypesRowGeneratorTest.NOTES, row.getCell(3).toString());
	}
}
