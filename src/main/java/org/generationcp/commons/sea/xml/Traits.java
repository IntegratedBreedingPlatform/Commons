/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package org.generationcp.commons.sea.xml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.generationcp.commons.breedingview.xml.Trait;

@XmlRootElement(name = "Traits")
@XmlType(propOrder = {"traits"})
public class Traits implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<Trait> traits;

	@XmlElement(name = "Trait")
	public List<Trait> getTraits() {
		return this.traits;
	}

	public void setTraits(List<Trait> traits) {
		this.traits = traits;
	}

	public void add(Trait trait) {

		if (this.traits == null) {
			this.traits = new ArrayList<Trait>();
			this.traits.add(trait);
		} else {
			this.traits.add(trait);
		}

	}

}
