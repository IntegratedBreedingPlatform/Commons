
package org.generationcp.commons.service;

import org.generationcp.commons.exceptions.BreedingViewImportException;

import java.io.File;

public interface BreedingViewImportService {

	void importMeansData(File file, int studyId) throws BreedingViewImportException;

	void importSummaryStatisticsData(File file, int studyId) throws BreedingViewImportException;

	void importOutlierData(File file, int studyId) throws BreedingViewImportException;

}
