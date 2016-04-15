
package org.generationcp.commons.service;

/**
 * This bean is typically populated in Spring application context using values in crossing.properties file.
 */
public class GermplasmNamingProperties {

	private String germplasmOriginNurseriesDefault;
	private String germplasmOriginNurseriesWheat;
	private String germplasmOriginNurseriesMaize;

	private String germplasmOriginTrialsDefault;
	private String germplasmOriginTrialsWheat;
	private String germplasmOriginTrialsMaize;

	private String breedersCrossIDNursery;
	private String breedersCrossIDTrial;

	public String getGermplasmOriginNurseriesDefault() {
		return this.germplasmOriginNurseriesDefault;
	}

	public void setGermplasmOriginNurseriesDefault(final String germplasmOriginNurseriesDefault) {
		this.germplasmOriginNurseriesDefault = germplasmOriginNurseriesDefault;
	}

	public String getGermplasmOriginNurseriesWheat() {
		return this.germplasmOriginNurseriesWheat;
	}

	public void setGermplasmOriginNurseriesWheat(final String germplasmOriginNurseriesWheat) {
		this.germplasmOriginNurseriesWheat = germplasmOriginNurseriesWheat;
	}

	public String getGermplasmOriginNurseriesMaize() {
		return this.germplasmOriginNurseriesMaize;
	}

	public void setGermplasmOriginNurseriesMaize(final String germplasmOriginNurseriesMaize) {
		this.germplasmOriginNurseriesMaize = germplasmOriginNurseriesMaize;
	}

	public String getGermplasmOriginTrialsDefault() {
		return this.germplasmOriginTrialsDefault;
	}

	public void setGermplasmOriginTrialsDefault(final String germplasmOriginTrialsDefault) {
		this.germplasmOriginTrialsDefault = germplasmOriginTrialsDefault;
	}

	public String getGermplasmOriginTrialsWheat() {
		return this.germplasmOriginTrialsWheat;
	}

	public void setGermplasmOriginTrialsWheat(final String germplasmOriginTrialsWheat) {
		this.germplasmOriginTrialsWheat = germplasmOriginTrialsWheat;
	}

	public String getGermplasmOriginTrialsMaize() {
		return this.germplasmOriginTrialsMaize;
	}

	public void setGermplasmOriginTrialsMaize(final String germplasmOriginTrialsMaize) {
		this.germplasmOriginTrialsMaize = germplasmOriginTrialsMaize;
	}

	public String getBreedersCrossIDNursery(){
		return this.breedersCrossIDNursery;
	}

	public void setBreedersCrossIDNursery(final String breedersCrossIDNursery){
		this.breedersCrossIDNursery = breedersCrossIDNursery;
	}

	public String getBreedersCrossIDTrial(){
		return this.breedersCrossIDTrial;
	}

	public void setBreedersCrossIDTrial(final String breedersCrossIDTrial){
		this.breedersCrossIDTrial = breedersCrossIDTrial;
	}

}
