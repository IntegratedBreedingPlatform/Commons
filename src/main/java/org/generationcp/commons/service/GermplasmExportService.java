
package org.generationcp.commons.service;

import org.generationcp.commons.exceptions.GermplasmListExporterException;
import org.generationcp.commons.pojo.ExportColumnHeader;
import org.generationcp.commons.pojo.ExportRow;
import org.generationcp.commons.pojo.GermplasmListExportInputValues;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Service to export Germplasm workbook as a file. Delegates to the GermplasmExportedWorkbook as the central builder,
 * then assembling an Excel style workbook as a Collection of ExcelWorkbookRow and ExcelCellStyleBuilder instances.
 * 
 * CSV output methods are used for the CSV output format for all Excel output and print style for label printers
 * 
 */
public interface GermplasmExportService {

	File generateCSVFile(List<ExportRow> exportRows, List<ExportColumnHeader> exportColumnHeaders,
		String fileNameFullPath) throws IOException;

	File generateCSVFile(List<ExportRow> exportRows, List<ExportColumnHeader> exportColumnHeaders,
		String fileNameFullPath, boolean includeHeader) throws IOException;

	FileOutputStream generateGermplasmListExcelFile(GermplasmListExportInputValues input) throws GermplasmListExporterException;
}
