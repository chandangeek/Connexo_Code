package com.elster.jupiter.fileimport.impl;

import java.io.File;
import java.util.Iterator;

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
        for (Iterator<File> files = scanner.getFiles(); files.hasNext();) {
            handler.handle(files.next());
        }
    }

}
