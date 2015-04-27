/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/
package org.generationcp.commons.service.impl;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.generationcp.commons.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * The Class FileServiceImpl.
 */
public class FileServiceImpl implements FileService{
    
    private static final Logger LOG = LoggerFactory.getLogger(FileServiceImpl.class);

    /** The upload directory. */
    private String uploadDirectory;

    /**
     * Instantiates a new file service impl.
     *
     * @param uploadDirectory the upload directory
     */
    public FileServiceImpl(String uploadDirectory) {
        this.uploadDirectory = uploadDirectory;
    }

    /* (non-Javadoc)
     * @see org.generationcp.commons.service.FileService#saveTemporaryFile(java.io.InputStream)
     */
    @Override
    public String saveTemporaryFile(InputStream userFile) throws IOException {
        String tempFileName = RandomStringUtils.randomAlphanumeric(15);

        File file = null;
        FileOutputStream fos = null;
        try {
            file = new File(getFilePath(tempFileName));
            file.createNewFile();
            fos = new FileOutputStream(file);
            int bytes = IOUtils.copy(userFile, fos);

        } finally {
            IOUtils.closeQuietly(fos);
        }

        return tempFileName;

    }

    /**
     * Gets the file path.
     *
     * @param tempFilename the temp filename
     * @return the file path
     */
    @Override
    public String getFilePath(String tempFilename) {
        return uploadDirectory + File.separator + tempFilename;
    }

    /* (non-Javadoc)
     * @see org.generationcp.commons.service.FileService#retrieveFileFromFileName(java.lang.String)
     */
    @Override
    public File retrieveFileFromFileName(String currentFilename) throws IOException {
        return new File(getFilePath(currentFilename));

    }

    public void init() {
        File file = new File(uploadDirectory);

        if (!file.exists()) {
            throw new BeanInitializationException(
                    "Specified upload directory does not exist : "
                            + uploadDirectory);
        }
    }
	
	 /* (non-Javadoc)
 	 * @see org.generationcp.commons.service.FileService#retrieveWorkbook(java.lang.String)
 	 */
 	@Override
    public Workbook retrieveWorkbook(String currentFilename)
			throws IOException, InvalidFormatException {
        File workbookFile = new File(getFilePath(currentFilename));

		return WorkbookFactory.create(workbookFile);

    }
}
