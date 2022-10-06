
package org.generationcp.commons.service;

import javax.xml.bind.JAXBException;

import org.generationcp.middleware.ruleengine.settings.PresetSetting;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */
public interface SettingsPresetService {

	public PresetSetting convertPresetFromXmlString(String presetXml, Class<? extends PresetSetting> targetClass) throws JAXBException;

	public String convertPresetSettingToXml(PresetSetting presetSetting, Class<? extends PresetSetting> targetClass) throws JAXBException;
}
