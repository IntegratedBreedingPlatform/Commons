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

import org.generationcp.middleware.ruleengine.pojo.ImportedCross;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Class ImportedCrossesList.
 */
public class ImportedCrossesList extends ImportedDescriptionDetails {

	private static final long serialVersionUID = 1L;

	/** The imported crosses. */
	protected List<ImportedCross> importedCrosses;

	protected Set<String> errorMessages = new HashSet<>();
	protected Set<String> warningMessages = new HashSet<>();

	/**
	 * Instantiates a new imported crosses list.
	 *
	 * @param filename the filename
	 * @param name the name
	 * @param title the title
	 * @param type the type
	 * @param date the date
	 */
	public ImportedCrossesList(final String filename, final String name, final String title, final String type, final Date date) {
		super(filename, name, title, type, date);
		this.importedCrosses = new ArrayList<>();
	}

	public ImportedCrossesList() {
		super();
		this.importedCrosses = new ArrayList<>();

	}

	/**
	 * Gets the imported crosses
	 *
	 * @return the imported crosses
	 */
	public List<ImportedCross> getImportedCrosses() {
		return this.importedCrosses;
	}

	/**
	 * Sets the imported crosses
	 *
	 * @param importedCrosses the new imported crosses
	 */
	public void setImportedGermplasms(final List<ImportedCross> importedCrosses) {
		this.importedCrosses = importedCrosses;
	}

	/**
	 * Adds the imported crosses
	 *
	 * @param importedCross the imported crosses
	 */
	public void addImportedCrosses(final ImportedCross importedCross) {
		this.importedCrosses.add(importedCross);
	}

	public Set<String> getErrorMessages() {
		return this.errorMessages;
	}

	public void addErrorMessages(final String errorMsg) {
		this.errorMessages.add(errorMsg);
	}

	public Set<String> getWarningMessages() {
		return this.warningMessages;
	}

	public void addWarningMessages(final String warningMsg) {
		this.warningMessages.add(warningMsg);
	}

	public boolean hasPlotDuplicate() {
		if (this.importedCrosses != null) {
			for (ImportedCross crosses : this.importedCrosses) {
				if (crosses.isPlotDupe()) {
					return true;
				}
			}
		}
		return false;
	}
}
