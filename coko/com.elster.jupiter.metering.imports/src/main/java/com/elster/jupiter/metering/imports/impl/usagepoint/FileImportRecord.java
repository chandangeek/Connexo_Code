package com.elster.jupiter.metering.imports.impl.usagepoint;


public abstract class FileImportRecord {

    private long lineNumber;

    public FileImportRecord() {
    }

    public FileImportRecord(long lineNumber) {
        this.lineNumber = lineNumber;
    }

    public void setLineNumber(long lineNumber) {
        this.lineNumber = lineNumber;
    }

    public long getLineNumber() {
        return lineNumber;
    }
}
