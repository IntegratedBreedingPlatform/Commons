
package org.generationcp.commons.parsing;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.service.api.user.UserDto;
import org.generationcp.middleware.service.api.user.UserService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class CrossesListDescriptionSheetParserTest {

	private final static String CROSSES_LIST_NO_LIST_DATE = "CrossesListNoListDate.xls";
	private final static String CROSSES_LIST = "CrossesList.xls";
	private static final String LIST_DATE_IN_XLS_TEST_FILE = "20160722";
	private static final String TEST_PERSON = "Test Person";

	private final ImportedCrossesList crossesList = new ImportedCrossesList();

	@Mock
	private UserService userService;

	private CrossesListDescriptionSheetParser<ImportedCrossesList> crossesListDescriptionSheetParser =
			new CrossesListDescriptionSheetParser<>(this.crossesList, this.userService);

	private Workbook workbookNoListDate;
	private Workbook workbook;
	private Date today;

	@Before
	public void setUp() throws Exception {
		final UserDto userTest = new UserDto();
		userTest.setId(1);
		Mockito.when(this.userService.countUsersByFullname(ArgumentMatchers.anyString())).thenReturn(new Long(1));
		Mockito.when(this.userService.getUserByFullname(ArgumentMatchers.anyString())).thenReturn(Optional.of(userTest));

		this.crossesListDescriptionSheetParser = new CrossesListDescriptionSheetParser<>(this.crossesList, this.userService);

		this.today = new Date();
		final URL crossesListWithoutDateURL =
				ClassLoader.getSystemClassLoader().getResource(CrossesListDescriptionSheetParserTest.CROSSES_LIST_NO_LIST_DATE);
		if (crossesListWithoutDateURL != null) {
			final File workbookFile1 = new File(crossesListWithoutDateURL.toURI());
			assert workbookFile1.exists();
			this.workbookNoListDate = WorkbookFactory.create(workbookFile1);
		}
		final URL crossesListURL = ClassLoader.getSystemClassLoader().getResource(CrossesListDescriptionSheetParserTest.CROSSES_LIST);
		if (crossesListURL != null) {
			final File workbookFile2 = new File(crossesListURL.toURI());
			assert workbookFile2.exists();
			this.workbook = WorkbookFactory.create(workbookFile2);
		}
	}

	@Test
	public void testEmptyListDate() throws FileParsingException {
		this.crossesListDescriptionSheetParser.parseWorkbook(this.workbookNoListDate, null);
		Assert.assertTrue("The list date should be " + this.today,
				this.crossesListDescriptionSheetParser.getImportedList().getDate().after(this.today));
	}

	@Test
	public void testListType() throws FileParsingException {
		this.crossesListDescriptionSheetParser.parseWorkbook(this.workbookNoListDate, null);
		Assert.assertEquals("The list type should be " + GermplasmListType.F1.name(),
				this.crossesListDescriptionSheetParser.getImportedList().getType(), GermplasmListType.F1.name());
	}

	@Test
	public void testWithListDate() throws ParseException, FileParsingException {
		this.crossesListDescriptionSheetParser.parseWorkbook(this.workbook, null);
		Assert.assertEquals("The list date should be " + CrossesListDescriptionSheetParserTest.LIST_DATE_IN_XLS_TEST_FILE,
			DateUtil.parseDate(CrossesListDescriptionSheetParserTest.LIST_DATE_IN_XLS_TEST_FILE), this.crossesListDescriptionSheetParser.getImportedList().getDate());
	}

	@Test
	public void testValidateListUserName() {
		Mockito.when(this.userService.countUsersByFullname(TEST_PERSON)).thenReturn(new Long(1));
		try {
			this.crossesListDescriptionSheetParser.validateListUserName(TEST_PERSON);
		} catch (final FileParsingException e) {
			Assert.fail("There should be no error.");
		}
	}

	@Test
	public void testValidateListUserNameInvalidListUser() {
		Mockito.when(this.userService.countUsersByFullname(TEST_PERSON)).thenReturn(new Long(0));
		try {
			this.crossesListDescriptionSheetParser.validateListUserName(TEST_PERSON);
			Assert.fail("There should an error since the method getPersonByFullName returned null.");
		} catch (final FileParsingException e) {
			Assert.assertEquals("The error message should be " + CrossesListDescriptionSheetParser.INVALID_LIST_USER,
					CrossesListDescriptionSheetParser.INVALID_LIST_USER, e.getMessage());
		}
	}

	@Test
	public void testValidateListUserNameMoreThanOneUser () {
		Mockito.when(this.userService.countUsersByFullname(TEST_PERSON)).thenReturn(new Long(2));
		try {
			this.crossesListDescriptionSheetParser.validateListUserName(TEST_PERSON);
			Assert.fail("There should an error since the method getPersonByFullName returned null.");
		} catch (final FileParsingException e) {
			Assert.assertEquals("The error message should be " + String.format(CrossesListDescriptionSheetParser.MORE_THAN_ONE_USER, TEST_PERSON),
				String.format(CrossesListDescriptionSheetParser.MORE_THAN_ONE_USER, TEST_PERSON), e.getMessage());
		}
	}
}
