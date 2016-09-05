package com.energyict.mdc.device.data.importers.impl.exceptions;

import com.elster.jupiter.util.exception.MessageSeed;

public class FileImportLineException extends ImportException {

    private long lineNumber;

    public FileImportLineException(long lineNumber, MessageSeed message, Object... args) {
        super(message, args);
        this.lineNumber = lineNumber;
    }

    public long getLineNumber(){
        return lineNumber;
    }
}