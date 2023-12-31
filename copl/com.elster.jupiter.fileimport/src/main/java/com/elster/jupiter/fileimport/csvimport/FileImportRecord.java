/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.csvimport;


public abstract class FileImportRecord {

    private long lineNumber;

    public FileImportRecord() {
    }

    public FileImportRecord(long lineNumber) {
        this.lineNumber = lineNumber;
    }

    public long getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(long lineNumber) {
        this.lineNumber = lineNumber;
    }
}
