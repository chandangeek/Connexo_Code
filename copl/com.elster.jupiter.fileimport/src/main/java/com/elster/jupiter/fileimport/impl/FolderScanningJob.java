package com.elster.jupiter.fileimport.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Runnable that passes the files of a FolderScanner to the configured FileHandler
 */
class FolderScanningJob implements Runnable {


    private static final Logger LOGGER = Logger.getLogger(FolderScanningJob.class.getName());


    private final FolderScanner scanner;
    private final FileHandler handler;

    public FolderScanningJob(FolderScanner scanner, FileHandler handler) {
        this.handler = handler;
        this.scanner = scanner;
    }

    @Override
    public void run() {
        try {
            scanner.getFiles().forEach(file -> handler.handle(file));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

}
