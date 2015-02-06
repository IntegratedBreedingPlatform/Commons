package org.generationcp.commons.service.impl;

import org.generationcp.commons.service.SettingsPresetService;
import org.generationcp.commons.settings.PresetSetting;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte

 */
public class SettingsPresetServiceImpl implements SettingsPresetService{
	@Override
	public PresetSetting convertPresetFromXmlString(String presetXml, Class<? extends PresetSetting> targetClass)
			throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(targetClass);
		Unmarshaller unmarshaller = context.createUnmarshaller();

		return targetClass
				.cast(unmarshaller.unmarshal(new StringReader(presetXml)));
	}

	public String convertPresetSettingToXml(PresetSetting presetSetting, Class<? extends PresetSetting> targetClass) throws JAXBException{
		JAXBContext context = JAXBContext.newInstance(targetClass);
		Marshaller marshaller = context.createMarshaller();
		StringWriter writer = new StringWriter();
		marshaller.marshal(presetSetting, writer);
		return writer.toString();
	}
}
