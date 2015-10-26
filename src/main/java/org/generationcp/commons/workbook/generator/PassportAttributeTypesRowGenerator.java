package org.generationcp.commons.workbook.generator;

import java.util.List;

import javax.annotation.Resource;

import org.apache.poi.ss.usermodel.CellStyle;
import org.generationcp.commons.parsing.ExcelCellStyleBuilder;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.UserDefinedField;

public class PassportAttributeTypesRowGenerator extends CodesSheetRowGenerator<UserDefinedField>{
	@Resource
	private GermplasmDataManager germplasmDataManager;
	
	@Override
	List<UserDefinedField> getSourceItem() {
		return this.germplasmDataManager.getUserDefinedFieldByFieldTableNameAndType(RowColumnType.PASSPORT_ATTRIBUTE_TYPES.getFtable(), RowColumnType.PASSPORT_ATTRIBUTE_TYPES.getFtype());
	}

	@Override
	CellStyle getLabelStyle() {
		return sheetStyles.getCellStyle(ExcelCellStyleBuilder.ExcelCellStyle.LABEL_STYLE_VARIATE);
	}

	@Override
	CellStyle getDataStyle() {
		return sheetStyles.getCellStyle(ExcelCellStyleBuilder.ExcelCellStyle.TEXT_DATA_FORMAT_STYLE);
	}

	@Override
	String getSection() {
		return RowColumnType.PASSPORT_ATTRIBUTE_TYPES.getSection();
	}

	@Override
	String getInfoType() {
		return RowColumnType.PASSPORT_ATTRIBUTE_TYPES.toString();
	}

	@Override
	String getFcode(UserDefinedField udField) {
		return udField.getFcode();
	}

	@Override
	String getFname(UserDefinedField udField) {
		return udField.getFname();
	}
}
