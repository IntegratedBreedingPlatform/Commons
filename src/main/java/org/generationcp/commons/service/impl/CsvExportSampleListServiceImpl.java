package org.generationcp.commons.service.impl;

import au.com.bytecode.opencsv.CSVWriter;
import org.apache.commons.lang.StringUtils;
import org.generationcp.commons.pojo.ExportColumnHeader;
import org.generationcp.commons.pojo.ExportRow;
import org.generationcp.commons.pojo.FileExportInfo;
import org.generationcp.commons.service.CsvExportSampleListService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.FileNameGenerator;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.commons.util.InstallationDirectoryUtil;
import org.generationcp.middleware.domain.sample.SampleDetailsDTO;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Service
@Transactional
public class CsvExportSampleListServiceImpl implements CsvExportSampleListService {

	private static final String FILE_EXTENSION = ".csv";
	private static final Logger LOG = LoggerFactory.getLogger(CsvExportSampleListServiceImpl.class);

	public static final String SAMPLE_ENTRY = "SAMPLE_ENTRY";
	public static final String DESIGNATION = "DESIGNATION";
	public static final String PLOT_NO = "PLOT_NO";
	public static final String SAMPLE_NAME = "SAMPLE_NAME";
	public static final String TAKEN_BY = "TAKEN_BY";
	public static final String SAMPLING_DATE = "SAMPLING_DATE";
	public static final String SAMPLE_UID = "SAMPLE_UID";
	public static final String SAMPLE_NO = "SAMPLE_NO";
	public static final String OBS_UNIT_ID = "OBS_UNIT_ID";
	public static final String GID = "GID";
	public static final String PLATE_ID = "PLATE_ID";
	public static final String WELL = "WELL";

	public static final List<String> AVAILABLE_COLUMNS = Collections.unmodifiableList(Arrays
		.asList(SAMPLE_ENTRY, DESIGNATION, PLOT_NO, SAMPLE_NO, SAMPLE_NAME, TAKEN_BY, SAMPLING_DATE,
			SAMPLE_UID, PLATE_ID, WELL,
			OBS_UNIT_ID, GID));

	@Resource
	private ContextUtil contextUtil;

	private String enumeratorVariableName = "";

	private final InstallationDirectoryUtil installationDirectoryUtil = new InstallationDirectoryUtil();

	@Override
	public FileExportInfo export(final List<SampleDetailsDTO> sampleDetailsDTOs, final String filenameWithoutExtension,
		final List<String> visibleColumns, final String enumeratorVariableName) throws IOException {
		LOG.debug("Initialize export");
		this.enumeratorVariableName = enumeratorVariableName;

		final List<ExportColumnHeader> exportColumnHeaders = this.getExportColumnHeaders(visibleColumns);
		final List<ExportRow> exportRows = this.getExportColumnValues(exportColumnHeaders, sampleDetailsDTOs);

		final String cleanFilenameWithoutExtension =
			FileUtils.sanitizeFileName(FileNameGenerator.generateFileName(filenameWithoutExtension));
		final String filenamePath = this.installationDirectoryUtil
			.getTempFileInOutputDirectoryForProjectAndTool(cleanFilenameWithoutExtension, FILE_EXTENSION,
				this.contextUtil.getProjectInContext(), ToolName.FIELDBOOK_WEB);
		this.generateCSVFile(exportRows, exportColumnHeaders, filenamePath);

		LOG.debug("Finished export");

		return new FileExportInfo(filenamePath, cleanFilenameWithoutExtension + FILE_EXTENSION);
	}

	protected List<ExportRow> getExportColumnValues(final List<ExportColumnHeader> columnHeaders,
		final List<SampleDetailsDTO> sampleDetailsDTOs) {
		final List<ExportRow> exportRows = new ArrayList<>();
		int i = 1;
		for (final SampleDetailsDTO sampleDetailsDTO : sampleDetailsDTOs) {
			sampleDetailsDTO.setSampleEntryNo(i++);
			exportRows.add(this.getColumnValueMap(columnHeaders, sampleDetailsDTO));
		}

		return exportRows;

	}

	protected List<ExportColumnHeader> getExportColumnHeaders(final List<String> visibleColumns) {
		final List<ExportColumnHeader> exportColumnHeaders = new ArrayList<>();

		int i = 0;

		final List<String> availableColumns = new LinkedList<>(AVAILABLE_COLUMNS);
		if (!PLOT_NO.equals(this.enumeratorVariableName) && visibleColumns.contains(this.enumeratorVariableName)) {
			// Add enumerator column (DATE_NO, PLANT_NO, custom enumerator variable, etc.) after PLOT_NO column
			// Do not add if enumeratorVariableName is also PLOT_NO (parent observations)
			availableColumns.add(availableColumns.indexOf(PLOT_NO), this.enumeratorVariableName);
		}

		if (!visibleColumns.contains(SAMPLE_UID)) {
			visibleColumns.add(SAMPLE_UID);
		}
		for (final String column : availableColumns) {
			if (visibleColumns.contains(column)) {
				exportColumnHeaders.add(new ExportColumnHeader(i++, column, true));
			}
		}

		return exportColumnHeaders;
	}

	private ExportRow getColumnValueMap(final List<ExportColumnHeader> columns,
		final SampleDetailsDTO sampleDetailsDTO) {
		final ExportRow row = new ExportRow();

		for (final ExportColumnHeader column : columns) {
			final Integer id = column.getId();
			row.addColumnValue(id, this.getColumnValue(sampleDetailsDTO, column));
		}

		return row;
	}

	private String getColumnValue(final SampleDetailsDTO sampleDetailsDTO, final ExportColumnHeader column) {
		String columnValue = null;

		switch (column.getName()) {
			case SAMPLE_ENTRY:
				columnValue = sampleDetailsDTO.getSampleEntryNo().toString();
				break;
			case DESIGNATION:
				columnValue = sampleDetailsDTO.getDesignation();
				break;
			case PLOT_NO:
				columnValue = sampleDetailsDTO.getPlotNo();
				break;
			case SAMPLE_NO:
				columnValue = String.valueOf(sampleDetailsDTO.getSampleNumber());
				break;
			case SAMPLE_NAME:
				columnValue = sampleDetailsDTO.getSampleName();
				break;
			case TAKEN_BY:
				columnValue = sampleDetailsDTO.getTakenBy();
				break;
			case SAMPLING_DATE:
				columnValue = this.setSampleDateValue(column.getId(), sampleDetailsDTO.getSampleDate());
				break;
			case SAMPLE_UID:
				columnValue = sampleDetailsDTO.getSampleBusinessKey();
				break;
			case OBS_UNIT_ID:
				columnValue = sampleDetailsDTO.getObsUnitId();
				break;
			case GID:
				columnValue = String.valueOf(sampleDetailsDTO.getGid());
				break;
			case PLATE_ID:
				columnValue = sampleDetailsDTO.getPlateId();
				break;
			case WELL:
				columnValue = sampleDetailsDTO.getWell();
				break;
			default:
				break;
		}

		// get the value of enumerator column (DATE_NO, PLANT_NO, custom enumerator variable, etc.)
		if (StringUtils.isNotBlank(this.enumeratorVariableName) && column.getName() == this.enumeratorVariableName) {
			columnValue = String.valueOf(sampleDetailsDTO.getObservationUnitNumber());
		}
		return columnValue;
	}

	private String setSampleDateValue(final Integer id, final Date sampleDate) {
		if (null != sampleDate) {
			final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			return sdf.format(sampleDate);
		}
		return "-";
	}

	public File generateCSVFile(final List<ExportRow> exportRows, final List<ExportColumnHeader> exportColumnHeaders,
		final String fileNameFullPath) throws IOException {
		return this.generateCSVFile(exportRows, exportColumnHeaders, fileNameFullPath, true);
	}

	public File generateCSVFile(final List<ExportRow> exportRows, final List<ExportColumnHeader> exportColumnHeaders,
		final String fileNameFullPath, final boolean includeHeader) throws IOException {
		final File newFile = new File(fileNameFullPath);

		final CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(fileNameFullPath), StandardCharsets.UTF_8), ',');

		// feed in your array (or convert your data to an array)
		final List<String[]> rowValues = new ArrayList<>();
		if (includeHeader) {
			rowValues.add(this.getColumnHeaderNames(exportColumnHeaders));
		}
		for (final ExportRow row : exportRows) {
			rowValues.add(this.getColumnValues(row, exportColumnHeaders));
		}
		writer.writeAll(rowValues);
		writer.close();
		return newFile;
	}

	private String[] getColumnValues(final ExportRow row, final List<ExportColumnHeader> exportColumnHeaders) {
		final List<String> values = new ArrayList<>();
		for (final ExportColumnHeader exportColumnHeader : exportColumnHeaders) {
			if (exportColumnHeader.isDisplay()) {
				final String colName = row.getValueForColumn(exportColumnHeader.getId());
				values.add(colName);
			}
		}
		return values.toArray(new String[values.size()]);
	}

	private String[] getColumnHeaderNames(final List<ExportColumnHeader> exportColumnHeaders) {
		final List<String> values = new ArrayList<>();
		for (final ExportColumnHeader exportColumnHeader : exportColumnHeaders) {
			if (exportColumnHeader.isDisplay()) {
				values.add(exportColumnHeader.getName());
			}
		}
		return values.toArray(new String[values.size()]);
	}

	public void setContextUtil(final ContextUtil contextUtil) {
		this.contextUtil = contextUtil;
	}

	protected void setEnumeratorVariableName(final String enumeratorVariableName) {
		this.enumeratorVariableName = enumeratorVariableName;
	}

}
