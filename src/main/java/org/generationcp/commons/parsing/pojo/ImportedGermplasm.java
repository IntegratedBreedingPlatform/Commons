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

package org.generationcp.commons.parsing.pojo;

import java.io.Serializable;
import java.util.List;

import org.generationcp.middleware.pojos.Name;

/**
 * The Class ImportedGermplasm.
 */
public class ImportedGermplasm implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	private Integer crossListId;

	/** The entry id. */
	private Integer entryId;

	/** The desig. */
	private String desig;

	/** The gid. */
	private String gid;

	/** The cross. */
	private String cross;

	/** The source. */
	private String source;

	/** The entry code. */
	private String entryCode;

	/** The check. */
	private String check;

	/** The breeding method id. */
	private Integer breedingMethodId;

	/** Germplasm's GPID1 */
	private Integer gpid1;

	/** Germplasm's GPID2 */
	private Integer gpid2;

	/** Germplasm's Number of Parents */
	private Integer gnpgs;
	
	/** The maintenance group this germplasm belongs to */
	private Integer mgid;

	/** List of Names associated with the Germplasm */
	private List<Name> names;

	private Integer checkId;

	private String checkName;

	private Integer index;

	private String groupName;

	private String trialInstanceNumber;
	private String replicationNumber;

	public Integer getIndex() {
		return this.index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	/**
	 * Instantiates a new imported germplasm.
	 */
	public ImportedGermplasm() {

	}

	/**
	 * Instantiates a new imported germplasm.
	 *
	 * @param entryId the entry id
	 * @param desig the desig
	 * @param check the check
	 */
	public ImportedGermplasm(Integer entryId, String desig, String check) {
		this.entryId = entryId;
		this.desig = desig;
		this.check = check;
	}

	/**
	 * Instantiates a new imported germplasm.
	 *
	 * @param entryId the entry id
	 * @param desig the desig
	 * @param gid the gid
	 * @param cross the cross
	 * @param source the source
	 * @param entryCode the entry code
	 * @param check the check
	 */
	public ImportedGermplasm(Integer entryId, String desig, String gid, String cross, String source, String entryCode, String check) {
		this.entryId = entryId;
		this.desig = desig;
		this.gid = gid;
		this.cross = cross;
		this.source = source;
		this.entryCode = entryCode;
		this.check = check;
	}

	public ImportedGermplasm(Integer entryId, String desig, String gid, String cross, String source, String entryCode, String check,
			Integer breedingMethodId) {

		this(entryId, desig, gid, cross, source, entryCode, check);
		this.breedingMethodId = breedingMethodId;
	}

	/**
	 * Gets the gid.
	 *
	 * @return the gid
	 */
	public String getGid() {
		return this.gid;
	}

	/**
	 * Sets the gid.
	 *
	 * @param gid the new gid
	 */
	public void setGid(String gid) {
		this.gid = gid;
	}

	/**
	 * Gets the cross.
	 *
	 * @return the cross
	 */
	public String getCross() {
		return this.cross;
	}

	/**
	 * Sets the cross.
	 *
	 * @param cross the new cross
	 */
	public void setCross(String cross) {
		this.cross = cross;
	}

	/**
	 * Gets the source.
	 *
	 * @return the source
	 */
	public String getSource() {
		return this.source;
	}

	/**
	 * Sets the source.
	 *
	 * @param source the new source
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * Gets the entry code.
	 *
	 * @return the entry code
	 */
	public String getEntryCode() {
		return this.entryCode;
	}

	/**
	 * Sets the entry code.
	 *
	 * @param entryCode the new entry code
	 */
	public void setEntryCode(String entryCode) {
		this.entryCode = entryCode;
	}

	/**
	 * Gets the entry id.
	 *
	 * @return the entry id
	 */
	public Integer getEntryId() {
		return this.entryId;
	}

	/**
	 * Sets the entry id.
	 *
	 * @param entryId the new entry id
	 */
	public void setEntryId(Integer entryId) {
		this.entryId = entryId;
	}

	/**
	 * Gets the desig.
	 *
	 * @return the desig
	 */
	public String getDesig() {
		return this.desig;
	}

	/**
	 * Sets the desig.
	 *
	 * @param desig the new desig
	 */
	public void setDesig(String desig) {
		this.desig = desig;
	}

	/**
	 * Gets the check.
	 *
	 * @return the check
	 */
	public String getCheck() {
		return this.check;
	}

	/**
	 * Sets the check.
	 *
	 * @param check the new check
	 */
	public void setCheck(String check) {
		this.check = check;
	}

	/**
	 * @return the breedingMethodId
	 */
	public Integer getBreedingMethodId() {
		return this.breedingMethodId;
	}

	/**
	 * @param breedingMethodId the breedingMethodId to set
	 */
	public void setBreedingMethodId(Integer breedingMethodId) {
		this.breedingMethodId = breedingMethodId;
	}

	/**
	 * @return the gpid1
	 */
	public Integer getGpid1() {
		return this.gpid1;
	}

	/**
	 * @param gpid1 the gpid1 to set
	 */
	public void setGpid1(Integer gpid1) {
		this.gpid1 = gpid1;
	}

	/**
	 * @return the gpid2
	 */
	public Integer getGpid2() {
		return this.gpid2;
	}

	/**
	 * @param gpid2 the gpid2 to set
	 */
	public void setGpid2(Integer gpid2) {
		this.gpid2 = gpid2;
	}

	/**
	 * @return the gnpgs
	 */
	public Integer getGnpgs() {
		return this.gnpgs;
	}

	/**
	 * @param gnpgs the gnpgs to set
	 */
	public void setGnpgs(Integer gnpgs) {
		this.gnpgs = gnpgs;
	}

	
	public Integer getMgid() {
		return mgid;
	}

	
	public void setMgid(Integer mgid) {
		this.mgid = mgid;
	}

	/**
	 * @return the names
	 */
	public List<Name> getNames() {
		return this.names;
	}

	/**
	 * @param names the names to set
	 */
	public void setNames(List<Name> names) {
		this.names = names;
	}

	/**
	 * @return the checkId
	 */
	public Integer getCheckId() {
		return this.checkId;
	}

	/**
	 * @param checkId the checkId to set
	 */
	public void setCheckId(Integer checkId) {
		this.checkId = checkId;
	}

	/**
	 * @return the checkName
	 */
	public String getCheckName() {
		return this.checkName;
	}

	/**
	 * @param checkName the checkName to set
	 */
	public void setCheckName(String checkName) {
		this.checkName = checkName;
	}

	public String getTrialInstanceNumber() {
		return this.trialInstanceNumber;
	}

	public void setTrialInstanceNumber(String trialInstanceNumber) {
		this.trialInstanceNumber = trialInstanceNumber;
	}

	public String getReplicationNumber() {
		return this.replicationNumber;
	}

	public void setReplicationNumber(String replicationNumber) {
		this.replicationNumber = replicationNumber;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ImportedGermplasm [entryId=" + this.entryId + ", desig=" + this.desig + ", gid=" + this.gid + ", cross=" + this.cross
				+ ", source=" + this.source + ", entryCode=" + this.entryCode + ", check=" + this.check + ", breedingMethodId="
				+ this.breedingMethodId + ", gpid1=" + this.gpid1 + ", gpid2=" + this.gpid2 + ", gnpgs=" + this.gnpgs + ", names="
				+ this.names + "]";
	}

	public ImportedGermplasm copy() {
		ImportedGermplasm rec =
				new ImportedGermplasm(this.entryId, this.desig, this.gid, this.cross, this.source, this.entryCode, this.check,
						this.breedingMethodId);

		rec.setGpid1(this.gpid1);
		rec.setGpid2(this.gpid2);
		rec.setGnpgs(this.gnpgs);
		rec.setNames(this.names);
		rec.setCheckId(this.checkId);
		rec.setCheckName(this.checkName);
		rec.setIndex(this.index);

		return rec;
	}

	public String getGroupName() {
		return this.groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public Integer getId() {
		return this.crossListId;
	}

	public void setCrossListId(Integer crossListId) {
		this.crossListId = crossListId;
	}

}
