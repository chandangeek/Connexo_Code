package com.elster.jupiter.fileimport.impl;

/**
 * Runnable that passes the files of a FolderScanner to the configured FileHandler
 */
class FolderScanningJob implements Runnable {

    private final FolderScanner scanner;
    private final FileHandler handler;

    public FolderScanningJob(FolderScanner scanner, FileHandler handler) {
        this.handler = handler;
        this.scanner = scanner;
    }

    @Override
    public void run() {
    	scanner.getFiles().forEach(file -> handler.handle(file));        
    }

}
