/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImportService;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Runnable that passes the files of a FolderScanner to the configured FileHandler
 */
class FolderScanningJob implements Runnable {


    private static final Logger LOGGER = Logger.getLogger(FolderScanningJob.class.getName());


    private final FolderScanner scanner;
    private final FileHandler handler;
    private final FileImportService fileImportService;

    public FolderScanningJob(FolderScanner scanner, FileHandler handler, FileImportService fileImportService) {
        this.handler = handler;
        this.scanner = scanner;
        this.fileImportService = fileImportService;
    }

    @Override
    public void run() {
        try {
            if (fileImportService.getAppServerName().isPresent()) {
                scanner.getFiles().forEach(handler::handle);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

}
