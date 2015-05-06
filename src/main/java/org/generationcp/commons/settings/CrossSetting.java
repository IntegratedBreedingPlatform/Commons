package org.generationcp.commons.settings;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;

@XmlRootElement
public class CrossSetting implements Serializable,PresetSetting {
	
	public static final String CROSSING_MANAGER_TOOL_NAME = "crossing_manager";

	private static final long serialVersionUID = 905356968758567192L;

	private String name;
	private BreedingMethodSetting breedingMethodSetting;
	private CrossNameSetting crossNameSetting;
	private AdditionalDetailsSetting additionalDetailsSetting;
	private boolean preservePlotDuplicates;
	
	public CrossSetting(){
		
	}

	public CrossSetting(String name,
			BreedingMethodSetting breedingMethodSetting,
			CrossNameSetting crossNameSetting,
			AdditionalDetailsSetting additionalDetailsSetting) {
		super();
		this.name = name;
		this.breedingMethodSetting = breedingMethodSetting;
		this.crossNameSetting = crossNameSetting;
		this.additionalDetailsSetting = additionalDetailsSetting;
	}

	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlElement
	public BreedingMethodSetting getBreedingMethodSetting() {
		return breedingMethodSetting;
	}

	public void setBreedingMethodSetting(BreedingMethodSetting breedingMethodSetting) {
		this.breedingMethodSetting = breedingMethodSetting;
	}

	@XmlElement
	public CrossNameSetting getCrossNameSetting() {
		return crossNameSetting;
	}

	public void setCrossNameSetting(CrossNameSetting crossNameSetting) {
		this.crossNameSetting = crossNameSetting;
	}

	@XmlElement
	public AdditionalDetailsSetting getAdditionalDetailsSetting() {
		return additionalDetailsSetting;
	}

	public void setAdditionalDetailsSetting(
			AdditionalDetailsSetting additionalDetailsSetting) {
		this.additionalDetailsSetting = additionalDetailsSetting;
	}		
	
	public boolean isPreservePlotDuplicates() {
		return preservePlotDuplicates;
	}

	public void setPreservePlotDuplicates(boolean preservePlotDuplicates) {
		this.preservePlotDuplicates = preservePlotDuplicates;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CrossSetting)) {
            return false;
        }

        CrossSetting rhs = (CrossSetting) obj;
        return new EqualsBuilder()
        		.append(name, rhs.name)
        		.append(breedingMethodSetting, rhs.breedingMethodSetting)
        		.append(crossNameSetting, rhs.crossNameSetting)
        		.append(additionalDetailsSetting, rhs.additionalDetailsSetting)
        		.isEquals();
    }
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((additionalDetailsSetting == null) ? 0 : additionalDetailsSetting.hashCode());
		result = prime * result
				+ ((breedingMethodSetting == null) ? 0 : breedingMethodSetting.hashCode());
		result = prime * result + ((crossNameSetting == null) ? 0 : crossNameSetting.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "CrossingManagerSetting [name=" + name
				+ ", breedingMethodSetting=" + breedingMethodSetting
				+ ", crossNameSetting=" + crossNameSetting
				+ ", additionalDetailsSetting=" + additionalDetailsSetting
				+ "]";
	}
	
}
