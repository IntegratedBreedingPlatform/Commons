
package org.generationcp.commons.service.impl;

import junit.framework.Assert;

import org.generationcp.commons.service.GermplasmNamingProperties;
import org.generationcp.commons.service.GermplasmOriginGenerationParameters;
import org.generationcp.middleware.domain.oms.StudyType;
import org.junit.Test;

public class GermplasmOriginGenerationServiceImplTest {

	@Test
	public void testDefaults() {

		final GermplasmNamingProperties profile = new GermplasmNamingProperties();
		profile.setGermplasmOriginNurseriesDefault("[NAME]:[PLOTNO]");
		profile.setGermplasmOriginNurseriesWheat("[NAME]:[PLOTNO]");
		profile.setGermplasmOriginNurseriesMaize("[NAME]:[PLOTNO]");

		final GermplasmOriginGenerationParameters parameters = new GermplasmOriginGenerationParameters();

		parameters.setStudyName("StudyName");
		parameters.setLocation("IND");
		parameters.setPlotNumber("1");

		final GermplasmOriginGenerationServiceImpl service = new GermplasmOriginGenerationServiceImpl();
		service.setGermplasmNamingProperties(profile);

		parameters.setStudyType(StudyType.N);
		parameters.setCrop("rice");
		Assert.assertEquals("StudyName:1", service.generateOriginString(parameters));

		parameters.setCrop("wheat");
		Assert.assertEquals("StudyName:1", service.generateOriginString(parameters));

		parameters.setCrop("maize");
		Assert.assertEquals("StudyName:1", service.generateOriginString(parameters));
	}

	@Test
	public void testDefaultsForCrossListImport() {

		final GermplasmNamingProperties profile = new GermplasmNamingProperties();
		profile.setGermplasmOriginNurseriesDefault("[NAME]:[PLOTNO]");
		profile.setGermplasmOriginNurseriesWheat("[NAME]:[PLOTNO]");
		profile.setGermplasmOriginNurseriesMaize("[NAME]:[PLOTNO]");

		final GermplasmOriginGenerationParameters parameters = new GermplasmOriginGenerationParameters();
		parameters.setCross(true);
		parameters.setMaleStudyName("Male Study Name");
		parameters.setMalePlotNumber("1");
		parameters.setFemaleStudyName("Female Study Name");
		parameters.setFemalePlotNumber("2");

		final GermplasmOriginGenerationServiceImpl service = new GermplasmOriginGenerationServiceImpl();
		service.setGermplasmNamingProperties(profile);

		parameters.setStudyType(StudyType.N);
		parameters.setCrop("rice");
		Assert.assertEquals("Female Study Name:2/Male Study Name:1", service.generateOriginString(parameters));

		parameters.setCrop("wheat");
		Assert.assertEquals("Female Study Name:2/Male Study Name:1", service.generateOriginString(parameters));

		parameters.setCrop("maize");
		Assert.assertEquals("Female Study Name:2/Male Study Name:1", service.generateOriginString(parameters));
	}

	@Test
	public void testWheat() {

		final GermplasmNamingProperties profile = new GermplasmNamingProperties();
		profile.setGermplasmOriginNurseriesWheat("[LOCATION]\\[SEASON]\\[NAME]\\[PLOTNO]");

		final GermplasmOriginGenerationParameters parameters = new GermplasmOriginGenerationParameters();
		parameters.setCrop("wheat");
		parameters.setStudyName("Wheat Study");
		parameters.setLocation("IND");
		parameters.setPlotNumber("1");
		parameters.setSeason("Summer");
		parameters.setStudyType(StudyType.N);

		final GermplasmOriginGenerationServiceImpl service = new GermplasmOriginGenerationServiceImpl();
		service.setGermplasmNamingProperties(profile);

		Assert.assertEquals("IND\\Summer\\Wheat Study\\1", service.generateOriginString(parameters));
	}

	@Test
	public void testWheatForCrossListImport() {

		final GermplasmNamingProperties profile = new GermplasmNamingProperties();
		profile.setGermplasmOriginNurseriesWheat("[LOCATION]\\[SEASON]\\[NAME]\\[PLOTNO]");

		final GermplasmOriginGenerationParameters parameters = new GermplasmOriginGenerationParameters();
		parameters.setCrop("wheat");
		parameters.setLocation("IND");
		parameters.setSeason("Summer");
		parameters.setCross(true);
		parameters.setMaleStudyName("Male Study Name");
		parameters.setMalePlotNumber("1");
		parameters.setFemaleStudyName("Female Study Name");
		parameters.setFemalePlotNumber("2");
		parameters.setStudyType(StudyType.N);

		final GermplasmOriginGenerationServiceImpl service = new GermplasmOriginGenerationServiceImpl();
		service.setGermplasmNamingProperties(profile);

		Assert.assertEquals("IND\\Summer\\Female Study Name\\2/IND\\Summer\\Male Study Name\\1", service.generateOriginString(parameters));
	}

	@Test
	public void testMaize() {

		final GermplasmNamingProperties profile = new GermplasmNamingProperties();
		profile.setGermplasmOriginNurseriesMaize("[LOCATION][SEASON]-[NAME]-[PLOTNO][SELECTION_NUMBER]");
		profile.setGermplasmOriginTrialsMaize("[LOCATION][SEASON]-[NAME]-[PLOTNO][SELECTION_NUMBER]");

		final GermplasmOriginGenerationParameters parameters = new GermplasmOriginGenerationParameters();
		parameters.setCrop("maize");
		parameters.setStudyName("Maize Study");
		parameters.setLocation("IND");
		parameters.setPlotNumber("12");
		parameters.setSeason("Winter");
		parameters.setSelectionNumber("2");

		final GermplasmOriginGenerationServiceImpl service = new GermplasmOriginGenerationServiceImpl();
		service.setGermplasmNamingProperties(profile);

		parameters.setStudyType(StudyType.N);
		Assert.assertEquals("INDWinter-Maize Study-12-2", service.generateOriginString(parameters));

		parameters.setStudyType(StudyType.T);
		Assert.assertEquals("INDWinter-Maize Study-12-2", service.generateOriginString(parameters));
	}
	
	@Test
	public void testMaizeNoSelectionNumber() {

		final GermplasmNamingProperties profile = new GermplasmNamingProperties();
		profile.setGermplasmOriginNurseriesMaize("[LOCATION][SEASON]-[NAME]-[PLOTNO][SELECTION_NUMBER]");
		profile.setGermplasmOriginTrialsMaize("[LOCATION][SEASON]-[NAME]-[PLOTNO][SELECTION_NUMBER]");

		final GermplasmOriginGenerationParameters parameters = new GermplasmOriginGenerationParameters();
		parameters.setCrop("maize");
		parameters.setStudyName("Maize Study");
		parameters.setLocation("IND");
		parameters.setPlotNumber("12");
		parameters.setSeason("Winter");
		//No selection number
		parameters.setSelectionNumber(null);
		parameters.setStudyType(StudyType.N);

		final GermplasmOriginGenerationServiceImpl service = new GermplasmOriginGenerationServiceImpl();
		service.setGermplasmNamingProperties(profile);

		parameters.setStudyType(StudyType.N);
		Assert.assertEquals("INDWinter-Maize Study-12", service.generateOriginString(parameters));

		parameters.setStudyType(StudyType.T);
		Assert.assertEquals("INDWinter-Maize Study-12", service.generateOriginString(parameters));
	}

	@Test
	public void testMaizeForCrosses() {

		final GermplasmNamingProperties profile = new GermplasmNamingProperties();
		profile.setGermplasmOriginNurseriesMaize("[LOCATION][SEASON]-[NAME]-[PLOTNO]");

		final GermplasmOriginGenerationParameters parameters = new GermplasmOriginGenerationParameters();
		parameters.setCrop("maize");
		parameters.setLocation("IND");
		parameters.setSeason("Summer");
		parameters.setCross(true);
		parameters.setMaleStudyName("Male Study Name");
		parameters.setMalePlotNumber("1");
		parameters.setFemaleStudyName("Female Study Name");
		parameters.setFemalePlotNumber("2");
		parameters.setStudyType(StudyType.N);

		final GermplasmOriginGenerationServiceImpl service = new GermplasmOriginGenerationServiceImpl();
		service.setGermplasmNamingProperties(profile);

		Assert.assertEquals("INDSummer-Female Study Name-2/INDSummer-Male Study Name-1", service.generateOriginString(parameters));
	}

	/**
	 * [SELECTION_NUMBER] present in template but no value specified.
	 */
	@Test
	public void testMaizeForCrossesWithSelectionNumberInTemplateOnly() {

		final GermplasmNamingProperties profile = new GermplasmNamingProperties();
		profile.setGermplasmOriginNurseriesMaize("[LOCATION][SEASON]-[NAME]-[PLOTNO][SELECTION_NUMBER]");

		final GermplasmOriginGenerationParameters parameters = new GermplasmOriginGenerationParameters();
		parameters.setCrop("maize");
		parameters.setLocation("IND");
		parameters.setSeason("Summer");
		parameters.setCross(true);
		parameters.setMaleStudyName("Male Study Name");
		parameters.setMalePlotNumber("1");
		parameters.setFemaleStudyName("Female Study Name");
		parameters.setFemalePlotNumber("2");
		parameters.setStudyType(StudyType.N);

		final GermplasmOriginGenerationServiceImpl service = new GermplasmOriginGenerationServiceImpl();
		service.setGermplasmNamingProperties(profile);

		Assert.assertEquals("INDSummer-Female Study Name-2/INDSummer-Male Study Name-1", service.generateOriginString(parameters));
	}

	/**
	 * [SELECTION_NUMBER] present in template value is also in specified parameters. Probably not a likely scenario in real life but service
	 * handles (better than always null/empty it) it so we must test the behavior.
	 */
	@Test
	public void testMaizeForCrossesWithSelectionNumberInTemplateAndValue() {

		final GermplasmNamingProperties profile = new GermplasmNamingProperties();
		profile.setGermplasmOriginNurseriesMaize("[LOCATION][SEASON]-[NAME]-[PLOTNO][SELECTION_NUMBER]");

		final GermplasmOriginGenerationParameters parameters = new GermplasmOriginGenerationParameters();
		parameters.setCrop("maize");
		parameters.setLocation("IND");
		parameters.setSeason("Summer");
		parameters.setCross(true);
		parameters.setMaleStudyName("Male Study Name");
		parameters.setMalePlotNumber("1");
		parameters.setFemaleStudyName("Female Study Name");
		parameters.setFemalePlotNumber("2");
		parameters.setStudyType(StudyType.N);
		parameters.setSelectionNumber("2");

		final GermplasmOriginGenerationServiceImpl service = new GermplasmOriginGenerationServiceImpl();
		service.setGermplasmNamingProperties(profile);

		Assert.assertEquals("INDSummer-Female Study Name-2-2/INDSummer-Male Study Name-1-2", service.generateOriginString(parameters));
	}

}