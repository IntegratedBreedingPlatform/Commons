
package org.generationcp.commons.parsing.pojo;

import org.generationcp.middleware.pojos.germplasm.GermplasmParent;

public class ImportedGermplasmParent extends GermplasmParent {

	private static final long serialVersionUID = 1L;

	private String plotNo;

	private String studyName;

	private String cross;

	public ImportedGermplasmParent(final Integer gid, final String designation, final String pedigree) {
		super(gid, designation, pedigree);
	}

	public ImportedGermplasmParent(final Integer gid, final String designation, final String plotNo, final String studyName) {
		super(gid, designation, null);
		this.plotNo = plotNo;
		this.studyName = studyName;
	}

	public String getPlotNo() {
		return this.plotNo;
	}

	public void setPlotNo(final String plotNo) {
		this.plotNo = plotNo;
	}

	public String getStudyName() {
		return this.studyName;
	}

	public void setStudyName(final String studyName) {
		this.studyName = studyName;
	}

	public String getCross() {
		return this.cross;
	}

	public void setCross(final String cross) {
		this.cross = cross;
	}

	@Override
	public String toString() {
		return "ImportedGermplasmParent [plotNo=" + this.plotNo + ", studyName=" + this.studyName + ", cross=" + this.cross + ", gid="
				+ this.gid + ", designation=" + this.designation + ", pedigree=" + this.pedigree + "]";
	}

}
