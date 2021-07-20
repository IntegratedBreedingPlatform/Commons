/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *******************************************************************************/

package org.generationcp.commons.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.BitSet;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.io.FilenameUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

public class FileUtils {

	public static final String INVALID_CHARACTER_REGEX_PATTERN = "[\\\\/:*?|<>\"]";
	public static final Character INVALID_FILE_CHARACTER_REPLACEMENT = '_';
	public static final String MIME_MS_EXCEL = "application/vnd.ms-excel";
	public static final String MIME_CSV = "text/csv";
	public static final String MIME_ZIP = "application/zip";
	public static final String MIME_PDF = "application/pdf";
	public static final String MIME_DEFAULT = "application/octet-stream";
	public static final String CONTENT_TYPE = "Content-Type";
	public static final String CONTENT_DISPOSITION = "Content-Disposition";

	/**
	 * BitSet of www-form-url safe characters.
	 */
	static final BitSet WWW_FORM_URL = new BitSet(256);

	// Static initializer for www_form_url
	static {
		// alpha characters
		for (int i = 'a'; i <= 'z'; i++) {
			WWW_FORM_URL.set(i);
		}
		for (int i = 'A'; i <= 'Z'; i++) {
			WWW_FORM_URL.set(i);
		}
		// numeric characters
		for (int i = '0'; i <= '9'; i++) {
			WWW_FORM_URL.set(i);
		}
		// special chars
		WWW_FORM_URL.set('-');
		WWW_FORM_URL.set('_');
		WWW_FORM_URL.set('.');
		WWW_FORM_URL.set('*');
		WWW_FORM_URL.set('/');
		WWW_FORM_URL.set(':');
	}

	private FileUtils() {
		// hide public constructor for this utility class
	}

	public static boolean isFilenameValid(String proposedFileName) {
		// blank file names are invalid regardless of OS
		if (StringUtils.isEmpty(proposedFileName)) {
			return false;
		}

		// files ending with dot are invalid
		if (proposedFileName.endsWith(".")) {
			return false;
		}

		return !proposedFileName.matches(".*" + INVALID_CHARACTER_REGEX_PATTERN + ".*");

	}

	/**
	 * Removes all forbidden characters in a file name.
	 *
	 * @param fileName
	 * @return
	 */
	public static String sanitizeFileName(String fileName) {

		String sanitizedFileName = fileName;

		sanitizedFileName = sanitizedFileName.replaceAll(INVALID_CHARACTER_REGEX_PATTERN, INVALID_FILE_CHARACTER_REPLACEMENT.toString());

		if (sanitizedFileName.endsWith(".")) {
			int index = sanitizedFileName.lastIndexOf('.');
			sanitizedFileName = sanitizedFileName.substring(0, index) + INVALID_FILE_CHARACTER_REPLACEMENT.toString();
		}

		return sanitizedFileName;
	}

	/**
	 * Encodes the file name to ASCII String
	 *
	 * @param filename
	 * @return
	 */
	public static String encodeFilenameForDownload(String filename) {
		String encodedUrl = null;
		if (filename != null) {
			encodedUrl = new String(URLCodec.encodeUrl(WWW_FORM_URL,
					filename.getBytes()));
		}
		return encodedUrl;
	}

	/**
	 * Get filename Extension
	 *
	 * @param f
	 * @return
	 */
	public static String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).toLowerCase();
		}

		if (ext == null) {
			return "";
		}
		return ext;
	}

	/**
	 * Get filename Extension
	 *
	 * @param f
	 * @return
	 */
	public static String getFilenameWithoutExtension(File f) {
		return FileUtils.getFilenameWithoutExtension(f.getName());
	}

	/**
	 * Get filename Extension
	 *
	 * @param completeFileName
	 * @return
	 */
	public static String getFilenameWithoutExtension(String completeFileName) {
		String filename = null;
		int i = completeFileName.lastIndexOf('.');

		if (i > 0 && i < completeFileName.length() - 1) {
			filename = completeFileName.substring(0, i);
		}

		if (filename == null) {
			return "";
		}
		return filename;
	}

	/*
	 * Gets the MIME Type of the file based on name
	 */
	public static String detectMimeType(String fileName) {

		String extension = FilenameUtils.getExtension(fileName);

		switch (extension) {
			case "xls":
				return MIME_MS_EXCEL;
			case "xlsx":
				return MIME_MS_EXCEL;
			case "zip":
				return MIME_ZIP;
			case "pdf":
				return MIME_PDF;
			default:
				return new MimetypesFileTypeMap().getContentType(fileName);
		}

	}

	public static String cleanFileName(final String name) {
		String finalName = name;
		if (finalName == null) {
			return null;
		}
		finalName = finalName.replaceAll("[:\\\\/*?|<>]", "_");
		return finalName;
	}

	public static MultipartFile wrapAsMultipart(final byte[] bytes) {
		return new MultipartFile() {

			@Override
			public String getName() {
				return null;
			}

			@Override
			public String getOriginalFilename() {
				return null;
			}

			@Override
			public String getContentType() {
				return null;
			}

			@Override
			public boolean isEmpty() {
				return bytes == null || bytes.length == 0;
			}

			@Override
			public long getSize() {
				return bytes.length;
			}

			@Override
			public byte[] getBytes() throws IOException {
				return bytes;
			}

			@Override
			public InputStream getInputStream() throws IOException {
				return new ByteArrayInputStream(bytes);
			}

			@Override
			public void transferTo(final File file) throws IOException, IllegalStateException {
				throw new UnsupportedOperationException();
			}
		};
	}
}
