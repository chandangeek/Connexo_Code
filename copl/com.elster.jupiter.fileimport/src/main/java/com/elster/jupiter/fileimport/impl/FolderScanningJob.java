package com.elster.jupiter.fileimport.impl;

public class FolderScanningJob implements Runnable {

    private final FolderScanner scanner;
    private final PathHandler handler;

    public FolderScanningJob(FolderScanner scanner, PathHandler handler) {
        this.handler = handler;
        this.scanner = scanner;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            handleNext();
        }
    }

    private void handleNext() {
        try {
            handler.handle(scanner.next());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
