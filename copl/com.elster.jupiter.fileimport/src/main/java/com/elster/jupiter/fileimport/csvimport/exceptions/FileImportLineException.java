/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.csvimport.exceptions;

import com.elster.jupiter.util.exception.MessageSeed;


public class FileImportLineException extends ImportException {

    private long lineNumber;

    public FileImportLineException(long lineNumber, MessageSeed message, Object... args) {
        super(message, args);
        this.lineNumber = lineNumber;
    }

    public long getLineNumber() {
        return lineNumber;
    }
}