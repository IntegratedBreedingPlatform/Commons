package org.generationcp.commons.service.impl;

import org.generationcp.middleware.exceptions.InvalidGermplasmNameSettingException;
import org.generationcp.middleware.pojos.germplasm.GermplasmNameSetting;
import org.generationcp.middleware.service.api.KeySequenceRegisterService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class GermplasmNamingServiceImplTest {

	private static final String PREFIX = "ABH";
	private static final String SUFFIX = "CDE";
	private static final Integer NEXT_NUMBER = 31;

	@Mock
	private KeySequenceRegisterService keySequenceRegisterService;

	@InjectMocks
	private GermplasmNamingServiceImpl germplasmNamingService;

	private GermplasmNameSetting germplasmNameSetting;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		this.germplasmNameSetting = this.createGermplasmNameSetting();
		Mockito.doReturn(NEXT_NUMBER).when(this.keySequenceRegisterService).getNextSequence(PREFIX);
	}


	@Test
	public void testBuildDesignationNameInSequenceDefaultSetting() {
		final GermplasmNameSetting defaultSetting = new GermplasmNameSetting();
		defaultSetting.setPrefix(GermplasmNamingServiceImplTest.PREFIX);
		defaultSetting.setSuffix(GermplasmNamingServiceImplTest.SUFFIX);
		defaultSetting.setAddSpaceBetweenPrefixAndCode(false);
		defaultSetting.setAddSpaceBetweenSuffixAndCode(false);

		final int nextNumber = 10;
		final String designationName = this.germplasmNamingService.buildDesignationNameInSequence(nextNumber, defaultSetting);
		Assert.assertEquals(PREFIX + nextNumber + SUFFIX, designationName);
	}

	@Test
	public void testBuildDesignationNameInSequenceWithSpacesInPrefixSuffix() {
		final int nextNumber = 10;
		final String designationName =
			this.germplasmNamingService.buildDesignationNameInSequence(nextNumber, this.germplasmNameSetting);
		Assert.assertEquals(PREFIX + " 00000" + nextNumber + " " + SUFFIX, designationName);
	}

	@Test
	public void testBuildPrefixStringDefault() {
		final GermplasmNameSetting setting = new GermplasmNameSetting();
		setting.setPrefix(" A  ");
		final String prefix = this.germplasmNamingService.buildPrefixString(setting);
		Assert.assertEquals("A", prefix);
	}

	@Test
	public void testBuildPrefixStringWithSpace() {
		final GermplasmNameSetting setting = new GermplasmNameSetting();
		setting.setPrefix("   A");
		setting.setAddSpaceBetweenPrefixAndCode(true);
		final String prefix = this.germplasmNamingService.buildPrefixString(setting);
		Assert.assertEquals("A ", prefix);
	}

	@Test
	public void testBuildSuffixStringDefault() {
		final GermplasmNameSetting setting = new GermplasmNameSetting();
		setting.setSuffix("  B   ");
		final String suffix = this.germplasmNamingService.buildSuffixString(setting);
		Assert.assertEquals("B", suffix);
	}

	@Test
	public void testBuildSuffixStringWithSpace() {
		final GermplasmNameSetting setting = new GermplasmNameSetting();
		setting.setSuffix("   B   ");
		setting.setAddSpaceBetweenSuffixAndCode(true);
		final String suffix = this.germplasmNamingService.buildSuffixString(setting);
		Assert.assertEquals(" B", suffix);
	}

	@Test
	public void testGetNextNumberInSequenceDefault() {
		final GermplasmNameSetting setting = new GermplasmNameSetting();
		setting.setPrefix(PREFIX);

		final int nextNumber = this.germplasmNamingService.getNextNumberInSequence(setting);
		Assert.assertEquals(GermplasmNamingServiceImplTest.NEXT_NUMBER.intValue(), nextNumber);
		final ArgumentCaptor<String> prefixCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(this.keySequenceRegisterService).getNextSequence(prefixCaptor.capture());
		Assert.assertEquals(PREFIX, prefixCaptor.getValue());
	}

	@Test
	public void testGetNextNumberInSequenceWhenPrefixIsEmpty() {

		final GermplasmNameSetting setting = new GermplasmNameSetting();
		setting.setStartNumber(1);
		setting.setPrefix("");

		final int nextNumber = this.germplasmNamingService.getNextNumberInSequence(setting);
		Assert.assertEquals(1, nextNumber);
		Mockito.verify(this.keySequenceRegisterService, Mockito.never()).getNextSequence(ArgumentMatchers.anyString());
	}

	@Test
	public void testGetNextNumberInSequenceWhenSpaceSuppliedBetweenPrefixAndCode() {
		final GermplasmNameSetting setting = new GermplasmNameSetting();
		final String prefix = "A";
		setting.setPrefix(prefix);
		setting.setAddSpaceBetweenPrefixAndCode(true);

		this.germplasmNamingService.getNextNumberInSequence(setting);
		final ArgumentCaptor<String> prefixCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(this.keySequenceRegisterService).getNextSequence(prefixCaptor.capture());
		Assert.assertEquals(prefix + " ", prefixCaptor.getValue());
	}

	@Test
	public void testGetNextNumberInSequenceWhenSpaceSuppliedBetweenSuffixAndCode() {
		final GermplasmNameSetting setting = new GermplasmNameSetting();
		final String prefix = "A";
		setting.setPrefix(prefix);
		final String suffix = "CDE";
		setting.setSuffix(suffix);
		setting.setAddSpaceBetweenSuffixAndCode(true);

		this.germplasmNamingService.getNextNumberInSequence(setting);
		final ArgumentCaptor<String> prefixCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(this.keySequenceRegisterService).getNextSequence(prefixCaptor.capture());
		Assert.assertEquals(prefix, prefixCaptor.getValue());
	}

	@Test
	public void testGetNextNumberInSequenceWhenSpaceSuppliedAfterPrefixAndBeforeSuffix() {
		final GermplasmNameSetting setting = new GermplasmNameSetting();
		final String prefix = "A";
		setting.setPrefix(prefix);
		setting.setAddSpaceBetweenPrefixAndCode(true);
		final String suffix = "CDE";
		setting.setSuffix(suffix);
		setting.setAddSpaceBetweenSuffixAndCode(true);

		this.germplasmNamingService.getNextNumberInSequence(setting);
		final ArgumentCaptor<String> prefixCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(this.keySequenceRegisterService).getNextSequence(prefixCaptor.capture());
		Assert.assertEquals(prefix + " ", prefixCaptor.getValue());
	}

	@Test
	public void testGetNumberWithLeadingZeroesAsStringDefault() {
		final GermplasmNameSetting setting = new GermplasmNameSetting();
		setting.setNumOfDigits(0);
		final String formattedString = this.germplasmNamingService.getNumberWithLeadingZeroesAsString(1, setting);
		Assert.assertEquals("1", formattedString);
	}

	@Test
	public void testGetNumberWithLeadingZeroesAsStringWithNumOfDigitsSpecified() {
		final GermplasmNameSetting setting = new GermplasmNameSetting();
		setting.setNumOfDigits(8);
		final String formattedString = this.germplasmNamingService.getNumberWithLeadingZeroesAsString(1, setting);
		Assert.assertEquals("00000001", formattedString);
	}

	@Test
	public void testGetNextNameInSequenceWithNullStartNumber() {
		String nextNameInSequence = "";
		try {
			nextNameInSequence = this.germplasmNamingService.getNextNameInSequence(this.germplasmNameSetting);
		} catch (final InvalidGermplasmNameSettingException e) {
			Assert.fail("Not expecting InvalidGermplasmNameSettingException to be thrown but was thrown.");
		}
		Assert.assertEquals(buildExpectedNextName(), nextNameInSequence);
	}

	@Test
	public void testGetNextNameInSequenceWithZeroStartNumber() {
		final GermplasmNameSetting setting = this.createGermplasmNameSetting();
		setting.setStartNumber(0);
		String nextNameInSequence = "";
		try {
			nextNameInSequence = this.germplasmNamingService.getNextNameInSequence(setting);
		} catch (final InvalidGermplasmNameSettingException e) {
			Assert.fail("Not expecting InvalidGermplasmNameSettingException to be thrown but was thrown.");
		}
		Assert.assertEquals(this.buildExpectedNextName(), nextNameInSequence);
	}

	private String buildExpectedNextName() {
		return GermplasmNamingServiceImplTest.PREFIX + " 000000" + NEXT_NUMBER + " "
			+ GermplasmNamingServiceImplTest.SUFFIX;
	}

	@Test
	public void testGetNextNameInSequenceWhenSpecifiedSequenceStartingNumberIsGreater() {
		final GermplasmNameSetting setting = this.createGermplasmNameSetting();
		final int startNumber = 1000;
		setting.setStartNumber(startNumber);
		String nextNameInSequence = "";
		try {
			nextNameInSequence = this.germplasmNamingService.getNextNameInSequence(setting);
		} catch (final InvalidGermplasmNameSettingException e) {
			Assert.fail("Not expecting InvalidGermplasmNameSettingException to be thrown but was thrown.");
		}
		Assert.assertEquals("The specified starting sequence number will be used since it's larger.",
			GermplasmNamingServiceImplTest.PREFIX + " 000" + startNumber + " " + GermplasmNamingServiceImplTest.SUFFIX,
			nextNameInSequence);
	}

	@Test
	public void testGetNextNameInSequenceWhenSpecifiedSequenceStartingNumberIsLower() {
		final GermplasmNameSetting setting = this.createGermplasmNameSetting();
		final int startNumber = GermplasmNamingServiceImplTest.NEXT_NUMBER - 1;
		setting.setStartNumber(startNumber);
		try {
			this.germplasmNamingService.getNextNameInSequence(setting);
			Assert.fail("Expecting InvalidGermplasmNameSettingException to be thrown but was not.");
		} catch (final InvalidGermplasmNameSettingException e) {
			Assert.assertEquals(
				"Starting sequence number should be higher than or equal to next name in the sequence: " + this.buildExpectedNextName()
					+ ".", e.getMessage());
		}
	}

	private GermplasmNameSetting createGermplasmNameSetting() {
		final GermplasmNameSetting setting = new GermplasmNameSetting();

		setting.setPrefix(GermplasmNamingServiceImplTest.PREFIX);
		setting.setSuffix(GermplasmNamingServiceImplTest.SUFFIX);
		setting.setAddSpaceBetweenPrefixAndCode(true);
		setting.setAddSpaceBetweenSuffixAndCode(true);
		setting.setNumOfDigits(7);

		return setting;
	}

}
