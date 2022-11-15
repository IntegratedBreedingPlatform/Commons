
package org.generationcp.commons.parsing;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.commons.parsing.pojo.ImportedCondition;
import org.generationcp.commons.parsing.pojo.ImportedDescriptionDetails;
import org.generationcp.commons.parsing.pojo.ImportedFactor;
import org.generationcp.commons.parsing.pojo.ImportedVariate;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.service.api.user.UserDto;
import org.generationcp.middleware.service.api.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class CrossesListDescriptionSheetParser<T extends ImportedDescriptionDetails> extends AbstractExcelFileParser<T> {

	static final String INVALID_LIST_USER = "The List User's value is invalid. See valid list user names on Codes sheet or leave it blank";

	static final String MORE_THAN_ONE_USER = "User named %s is not unique in the database. Please contact your administrator";

	private static final String DESCRIPTION_SHEET_NAME = "Description";

	private static final int CONDITION_ROW_NO = 4;
	private static final int DESCRIPTION_SHEET_COL_SIZE = 8;

	private static final Logger LOG = LoggerFactory.getLogger(CrossesListDescriptionSheetParser.class);
	public static final String LIST_DATE = "LIST DATE";
	public static final String LIST_TYPE = "LIST TYPE";

	private enum DescriptionHeaders {
		CONDITION("CONDITION"), DESCRIPTION("DESCRIPTION"), PROPERTY("PROPERTY"), SCALE("SCALE"), METHOD("METHOD"), DATA_TYPE(
				"DATA TYPE"), VALUE("VALUE"), FACTOR("FACTOR"), CONSTANT("CONSTANT"), VARIATE("VARIATE");

		private final String label;

		DescriptionHeaders(final String label) {
			this.label = label;
		}

		public String getLabel() {
			return this.label;
		}
	}

	private final T importedList;

	private int currentRow = 0;

	private final boolean doParseDetails;
	private final boolean doParseConditions;
	private final boolean doParseFactors;
	private final boolean doParseVariates;

	private final UserService userService;
	private Integer descriptionSheetIndex;

	public CrossesListDescriptionSheetParser(final T importedList, final UserService userService) {
		this.importedList = importedList;
		this.doParseDetails = true;
		this.doParseConditions = true;
		this.doParseFactors = true;
		this.doParseVariates = true;
		this.userService = userService;
		this.descriptionSheetIndex = 0;
	}

	private void parseDescriptionSheet() throws FileParsingException, ParseException {
		this.descriptionSheetIndex = this.getSheetIndex(CrossesListDescriptionSheetParser.DESCRIPTION_SHEET_NAME);
		this.parseDescriptionSheet(this.doParseDetails, this.doParseConditions, this.doParseFactors, this.doParseVariates);
	}

	private void parseDescriptionSheet(final boolean doParseDetails, final boolean doParseConditions, final boolean doParseFactors,
			final boolean doParseVariates) throws FileParsingException, ParseException {

		if (doParseDetails) {
			this.parseListDetails();
		}

		if (doParseConditions) {
			this.parseConditions();
		}

		if (doParseFactors) {
			this.parseFactors();
		}

		if (doParseVariates) {
			this.parseVariate();
		}
	}

	private void parseListDetails() throws FileParsingException, ParseException {

		final String listName = this.getCellStringValue(this.descriptionSheetIndex, 0, 1);
		this.importedList.setName(listName);

		final String listTitle = this.getCellStringValue(this.descriptionSheetIndex, 1, 1);
		this.importedList.setTitle(listTitle);

		// The list type for the crosses import will always be F1 list type
		this.importedList.setType(GermplasmListType.F1.name());

		final String labelId = this.getCellStringValue(this.descriptionSheetIndex, 2, 0);
		final int listDateColNo = CrossesListDescriptionSheetParser.LIST_DATE.equalsIgnoreCase(labelId) ? 2 : 3;
		final Date listDate;
		final Double listDateNotParsed = this.getCellNumericValue(this.descriptionSheetIndex, listDateColNo, 1);
		if (listDateNotParsed.equals(0d)) {
			listDate = DateUtil.getCurrentDate();
		} else {
			listDate = DateUtil.parseDate(String.valueOf(listDateNotParsed.intValue()));
		}

		this.importedList.setDate(listDate);

		final String listUserName = this.getCellStringValue(this.descriptionSheetIndex, 5, 6);
		this.validateListUserName(listUserName.trim());
	}

	void validateListUserName(final String listUserName) throws FileParsingException {
		if (StringUtils.isNotEmpty(listUserName)) {
			final Long numberOfUsersWithSpecifiedName = this.userService.countUsersByFullname(listUserName);
			if(numberOfUsersWithSpecifiedName == 0) {
				throw new FileParsingException(CrossesListDescriptionSheetParser.INVALID_LIST_USER);
			} else if(numberOfUsersWithSpecifiedName == 1) {
				final UserDto user = this.userService.getUserByFullname(listUserName);
				this.importedList.setUserId(user.getId());
			} else {
				throw new FileParsingException(String.format(CrossesListDescriptionSheetParser.MORE_THAN_ONE_USER, listUserName));
			}
		}
	}

	private void parseConditions() {
		// condition headers start at row = 4 (5 - 1 : count starts from 0 )
		this.currentRow = CrossesListDescriptionSheetParser.CONDITION_ROW_NO;

		if (!this.isConditionHeadersInvalid(CrossesListDescriptionSheetParser.CONDITION_ROW_NO)) {
			this.currentRow++;

			while (!this.isRowEmpty(this.descriptionSheetIndex, this.currentRow, CrossesListDescriptionSheetParser.DESCRIPTION_SHEET_COL_SIZE)) {
				this.importedList.addImportedCondition(new ImportedCondition(this.getCellStringValue(this.descriptionSheetIndex, this.currentRow, 0),
						this.getCellStringValue(this.descriptionSheetIndex, this.currentRow, 1),
						this.getCellStringValue(this.descriptionSheetIndex, this.currentRow, 2),
						this.getCellStringValue(this.descriptionSheetIndex, this.currentRow, 3),
						this.getCellStringValue(this.descriptionSheetIndex, this.currentRow, 4),
						this.getCellStringValue(this.descriptionSheetIndex, this.currentRow, 5),
						this.getCellStringValue(this.descriptionSheetIndex, this.currentRow, 6), ""));

				this.currentRow++;
			}
		}

		while (this.isRowEmpty(this.descriptionSheetIndex, this.currentRow, CrossesListDescriptionSheetParser.DESCRIPTION_SHEET_COL_SIZE)) {
			this.currentRow++;
		}
	}

	private void parseFactors() throws FileParsingException {

		if (!this.isFactorHeadersInvalid(this.currentRow)) {
			this.currentRow++;

			while (!this.isRowEmpty(this.descriptionSheetIndex, this.currentRow, CrossesListDescriptionSheetParser.DESCRIPTION_SHEET_COL_SIZE)) {
				final ImportedFactor factor = new ImportedFactor(this.getCellStringValue(this.descriptionSheetIndex, this.currentRow, 0),
						this.getCellStringValue(this.descriptionSheetIndex, this.currentRow, 1),
						this.getCellStringValue(this.descriptionSheetIndex, this.currentRow, 2),
						this.getCellStringValue(this.descriptionSheetIndex, this.currentRow, 3),
						this.getCellStringValue(this.descriptionSheetIndex, this.currentRow, 4),
						this.getCellStringValue(this.descriptionSheetIndex, this.currentRow, 5), "");

				this.importedList.addImportedFactor(factor);

				this.currentRow++;
			}

			this.currentRow++;

		} else {
			throw new FileParsingException("Error parsing on factors header: Incorrect headers for factors.");
		}

		while (this.isRowEmpty(this.descriptionSheetIndex, this.currentRow, CrossesListDescriptionSheetParser.DESCRIPTION_SHEET_COL_SIZE)) {
			this.currentRow++;
		}
	}

	private void parseVariate() throws FileParsingException {
		if (!this.isVariateHeaderInvalid(this.currentRow)) {
			this.currentRow++;
			while (!this.isRowEmpty(this.descriptionSheetIndex, this.currentRow, CrossesListDescriptionSheetParser.DESCRIPTION_SHEET_COL_SIZE)) {
				this.importedList.addImportedVariate(new ImportedVariate(this.getCellStringValue(this.descriptionSheetIndex, this.currentRow, 0),
						this.getCellStringValue(this.descriptionSheetIndex, this.currentRow, 1),
						this.getCellStringValue(this.descriptionSheetIndex, this.currentRow, 2),
						this.getCellStringValue(this.descriptionSheetIndex, this.currentRow, 3),
						this.getCellStringValue(this.descriptionSheetIndex, this.currentRow, 4),
						this.getCellStringValue(this.descriptionSheetIndex, this.currentRow, 5)));
				this.currentRow++;
			}

		} else {
			throw new FileParsingException("Error parsing on variates header: Incorrect headers for variates.");
		}
	}

	private boolean isConditionHeadersInvalid(final int conditionHeaderRowNo) {
		final String[] headers = {DescriptionHeaders.CONDITION.getLabel(), DescriptionHeaders.DESCRIPTION.getLabel(),
				DescriptionHeaders.PROPERTY.getLabel(), DescriptionHeaders.SCALE.getLabel(), DescriptionHeaders.METHOD.getLabel(),
				DescriptionHeaders.DATA_TYPE.getLabel(), DescriptionHeaders.VALUE.getLabel()};

		return this.isHeaderInvalid(conditionHeaderRowNo, this.descriptionSheetIndex, headers);
	}

	private boolean isFactorHeadersInvalid(final int factorHeaderRowNo) {
		final String[] headers =
				{DescriptionHeaders.FACTOR.getLabel(), DescriptionHeaders.DESCRIPTION.getLabel(), DescriptionHeaders.PROPERTY.getLabel(),
						DescriptionHeaders.SCALE.getLabel(), DescriptionHeaders.METHOD.getLabel(), DescriptionHeaders.DATA_TYPE.getLabel()};

		return this.isHeaderInvalid(factorHeaderRowNo, this.descriptionSheetIndex, headers);
	}

	private boolean isVariateHeaderInvalid(final int variateHeaderRowNo) {
		final String[] headers =
				{DescriptionHeaders.VARIATE.getLabel(), DescriptionHeaders.DESCRIPTION.getLabel(), DescriptionHeaders.PROPERTY.getLabel(),
						DescriptionHeaders.SCALE.getLabel(), DescriptionHeaders.METHOD.getLabel(), DescriptionHeaders.DATA_TYPE.getLabel()};

		return this.isHeaderInvalid(variateHeaderRowNo, this.descriptionSheetIndex, headers);
	}

	T getImportedList() {
		return this.importedList;
	}

	@Override
	public T parseWorkbook(final Workbook workbook, final Map<String, Object> addtlParams) throws FileParsingException {
		try {
			this.workbook = workbook;

			this.parseDescriptionSheet();
			return this.importedList;
		} catch (final ParseException e) {
			CrossesListDescriptionSheetParser.LOG.debug(e.getMessage(), e);
			throw new FileParsingException(
					this.messageSource.getMessage(AbstractExcelFileParser.FILE_INVALID, new Object[] {}, Locale.getDefault()));
		}
	}
}
