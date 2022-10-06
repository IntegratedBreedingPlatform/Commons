
package org.generationcp.commons.service.impl;

import javax.xml.bind.JAXBException;

import junit.framework.Assert;

import org.generationcp.commons.service.SettingsPresetService;
import org.generationcp.middleware.ruleengine.settings.BreedingMethodSetting;
import org.generationcp.middleware.ruleengine.settings.CrossNameSetting;
import org.generationcp.middleware.ruleengine.settings.CrossSetting;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */

public class SettingsPresetServiceTest {

	private SettingsPresetService dut;

	public static final String TEST_SETTING_NAME = "mySettingName";
	public static final Integer TEST_BREEDING_METHOD_ID = 1;
	public static final String SETTING_PREFIX = "PRE";
	public static final String SETTING_SEPARATOR = "-";

	@Before
	public void setup() {
		this.dut = new SettingsPresetServiceImpl();
	}

	@Test
	public void testConvertToXmlPositive() {
		CrossSetting setting = this.constructTestSettingObject();

		try {
			String xml = this.dut.convertPresetSettingToXml(setting, CrossSetting.class);

			Assert.assertTrue("Setting was not properly converted to XML",
					xml.contains("name=\"" + SettingsPresetServiceTest.TEST_SETTING_NAME + "\""));
			Assert.assertTrue("Setting was not properly converted to XML",
					xml.contains("methodId=\"" + SettingsPresetServiceTest.TEST_BREEDING_METHOD_ID + "\""));
			Assert.assertTrue("Setting was not properly converted to XML",
					xml.contains("prefix=\"" + SettingsPresetServiceTest.SETTING_PREFIX + "\""));
			Assert.assertTrue("Setting was not properly converted to XML",
					xml.contains("separator=\"" + SettingsPresetServiceTest.SETTING_SEPARATOR + "\""));
		} catch (JAXBException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testConvertSettingToObject() {

		try {
			String xml =
					"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><crossSetting name=\""
							+ SettingsPresetServiceTest.TEST_SETTING_NAME + "\">"
							+ "<breedingMethodSetting basedOnStatusOfParentalLines=\"false\" methodId=\""
							+ SettingsPresetServiceTest.TEST_BREEDING_METHOD_ID + "\"/>"
							+ "<crossNameSetting addSpaceBetweenPrefixAndCode=\"false\" addSpaceBetweenSuffixAndCode=\"false\" prefix=\""
							+ SettingsPresetServiceTest.SETTING_PREFIX + "\" separator=\"" + SettingsPresetServiceTest.SETTING_SEPARATOR
							+ "\"/>" + "</crossSetting>";
			CrossSetting converted = (CrossSetting) this.dut.convertPresetFromXmlString(xml, CrossSetting.class);

			Assert.assertEquals("Setting was not properly converted from XML", SettingsPresetServiceTest.TEST_SETTING_NAME,
					converted.getName());
			Assert.assertEquals("Setting was not properly converted from XML", SettingsPresetServiceTest.TEST_BREEDING_METHOD_ID, converted
					.getBreedingMethodSetting().getMethodId());
			Assert.assertEquals("Setting was not properly converted from XML", SettingsPresetServiceTest.SETTING_PREFIX, converted
					.getCrossNameSetting().getPrefix());
			Assert.assertEquals("Setting was not properly converted from XML", SettingsPresetServiceTest.SETTING_SEPARATOR, converted
					.getCrossNameSetting().getSeparator());
		} catch (JAXBException e) {
			e.printStackTrace();
		}

	}

	protected CrossSetting constructTestSettingObject() {
		CrossSetting setting = new CrossSetting();
		setting.setName(SettingsPresetServiceTest.TEST_SETTING_NAME);

		BreedingMethodSetting methodSetting = new BreedingMethodSetting(SettingsPresetServiceTest.TEST_BREEDING_METHOD_ID, false, false);
		setting.setBreedingMethodSetting(methodSetting);

		CrossNameSetting nameSetting = new CrossNameSetting();
		nameSetting.setPrefix(SettingsPresetServiceTest.SETTING_PREFIX);
		nameSetting.setSeparator(SettingsPresetServiceTest.SETTING_SEPARATOR);
		setting.setCrossNameSetting(nameSetting);

		return setting;
	}

}
