package org.generationcp.commons.parsing;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.pojo.GermplasmListExportInputValues;
import org.generationcp.middleware.domain.inventory.ListDataInventory;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.Method;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;

public class GermplasmExportTestHelper {

	public static final String CURRENT_USER_NAME = "User User";
	public static final int CURRENT_USER_ID = 1;
	public static final Integer USER_ID = 1;
	public static final int NO_OF_LIST_ENTRIES = 10;
	public static final String TEST_FILE_NAME = "test.csv";

	public static HSSFWorkbook createWorkbook() {
		final HSSFWorkbook wb = new HSSFWorkbook();
		wb.createSheet("Codes");
		return wb;
	}

	public static GermplasmListExportInputValues generateGermplasmListExportInputValues() {
		final GermplasmListExportInputValues input = new GermplasmListExportInputValues();

		input.setFileName(TEST_FILE_NAME);
		input.setGermplasmList(generateGermplasmList());
		input.setOwnerName(CURRENT_USER_NAME);
		input.setCurrentLocalIbdbUserId(CURRENT_USER_ID);
		input.setExporterName(CURRENT_USER_NAME);
		input.setVisibleColumnMap(getVisibleColumnMap());
		input.setColumnTermMap(getColumnTerms());
		input.setInventoryVariableMap(getInventoryVariables());
		input.setVariateVariableMap(getVariateVariables());
		input.setListData(generateListEntries());
		return input;
	}

	public static GermplasmList generateGermplasmList() {
		final GermplasmList germplasmList = new GermplasmList();
		germplasmList.setName("Sample List");
		germplasmList.setUserId(USER_ID);
		germplasmList.setDescription("Sample description");
		germplasmList.setType("LST");
		germplasmList.setDate(20141112L);
		germplasmList.setNotes("Sample Notes");
		germplasmList.setListData(generateListEntries());

		return germplasmList;
	}

	public static  List<GermplasmListData> generateListEntries() {
		final List<GermplasmListData> entries = new ArrayList<>();

		for (int x = 1; x <= NO_OF_LIST_ENTRIES; x++) {
			final GermplasmListData germplasmListData = new GermplasmListData();
			germplasmListData.setId(x);
			germplasmListData.setEntryId(x);
			germplasmListData.setDesignation(ColumnLabels.DESIGNATION.getName() + x);
			germplasmListData.setGroupName(ColumnLabels.PARENTAGE.getName() + x);
			final ListDataInventory inventoryInfo = new ListDataInventory(x, x);
			inventoryInfo.setLotCount(1);
			inventoryInfo.setReservedLotCount(1);
			inventoryInfo.setActualInventoryLotCount(1);
			germplasmListData.setInventoryInfo(inventoryInfo);
			germplasmListData.setEntryCode(ColumnLabels.ENTRY_CODE.getName() + x);
			germplasmListData.setSeedSource(ColumnLabels.SEED_SOURCE.getName() + x);
			germplasmListData.setGid(x);
			entries.add(germplasmListData);
		}

		return entries;
	}

	public static Map<String, Boolean> getVisibleColumnMap() {
		final Map<String, Boolean> visibleColumnMap = new LinkedHashMap<>();

		visibleColumnMap.put(String.valueOf(ColumnLabels.ENTRY_ID.getTermId().getId()), true);
		visibleColumnMap.put(String.valueOf(ColumnLabels.GID.getTermId().getId()), true);
		visibleColumnMap.put(String.valueOf(ColumnLabels.ENTRY_CODE.getTermId().getId()), true);
		visibleColumnMap.put(String.valueOf(ColumnLabels.DESIGNATION.getTermId().getId()), true);
		visibleColumnMap.put(String.valueOf(ColumnLabels.PARENTAGE.getTermId().getId()), true);
		visibleColumnMap.put(String.valueOf(ColumnLabels.SEED_SOURCE.getTermId().getId()), true);

		return visibleColumnMap;
	}

	public static Map<Integer, Term> getColumnTerms() {

		final Map<Integer, Term> termMap = new LinkedHashMap<>();

		termMap.put(TermId.ENTRY_NO.getId(),
				createVariable(TermId.ENTRY_NO.getId(), "ENTRY_NO", "Germplasm entry - enumerated (number)", "Germplasm entry",
						"Number", "Enumerated", DataType.NUMERIC_VARIABLE));
		termMap.put(TermId.GID.getId(),
				createVariable(TermId.GID.getId(), "GID", "Germplasm identifier - assigned (DBID)", "Germplasm id", "DBID", "Assigned",
						DataType.NUMERIC_VARIABLE));
		termMap.put(TermId.CROSS.getId(),
				createVariable(TermId.CROSS.getId(), "CROSS", "The pedigree string of the germplasm", "Cross history", "Text",
						"Assigned", DataType.CHARACTER_VARIABLE));
		termMap.put(TermId.ENTRY_CODE.getId(),
				createVariable(TermId.ENTRY_CODE.getId(), "ENTRY_CODE", "Germplasm ID - Assigned (Code)", "Germplasm entry", "Code",
						"Assigned", DataType.CHARACTER_VARIABLE));
		termMap.put(TermId.DESIG.getId(),
				createVariable(TermId.DESIG.getId(), "DESIGNATION", "Germplasm identifier - assigned (DBCV)", "Germplasm id", "DBCV",
						"Assigned", DataType.CHARACTER_VARIABLE));
		termMap.put(TermId.SEED_SOURCE.getId(),
				createVariable(TermId.SEED_SOURCE.getId(), "SEED_SOURCE", "Seed source - Selected (Code)", "Seed source", "Code",
						"Selected", DataType.CHARACTER_VARIABLE));

		return termMap;
	}

	public static  Variable createVariable(final int termId, final String name, final String description, final String property,
			final String scale, final String method, final DataType dataType) {
		final Variable stdvariable = new Variable();
		stdvariable.setId(termId);
		stdvariable.setName(name);
		stdvariable.setDefinition(description);
		stdvariable.setProperty(new Property(new Term(0, property, "")));
		stdvariable.setScale(new Scale(new Term(0, scale, "")));
		stdvariable.setMethod(new Method(new Term(0, method, "")));
		stdvariable.getScale().setDataType(dataType);
		return stdvariable;
	}

	public static Map<Integer, Variable> getVariateVariables() {
		final Map<Integer, Variable> standardVariableMap = new LinkedHashMap<>();

		standardVariableMap.put(TermId.NOTES.getId(),
				createVariable(TermId.NOTES.getId(), "NOTES", "Field notes - observed (text)", "Comment", "Text", "Observed",
						DataType.CHARACTER_VARIABLE));

		return standardVariableMap;
	}

	public static Map<Integer, Variable> getInventoryVariables() {

		final Map<Integer, Variable> standardVariableMap = new LinkedHashMap<>();

		standardVariableMap.put(TermId.STOCKID.getId(),
				createVariable(TermId.STOCKID.getId(), "stockID", "ID of an inventory deposit", "Germplasm stock id", "DBCV", "Assigned",
						DataType.CHARACTER_VARIABLE));
		standardVariableMap.put(TermId.SEED_AMOUNT_G.getId(),
				createVariable(TermId.SEED_AMOUNT_G.getId(), "SEED_AMOUNT_g", "Seed inventory amount deposited or withdrawn (g)",
						"Inventory amount", "g", "Weighed", DataType.CHARACTER_VARIABLE));

		return standardVariableMap;
	}

}