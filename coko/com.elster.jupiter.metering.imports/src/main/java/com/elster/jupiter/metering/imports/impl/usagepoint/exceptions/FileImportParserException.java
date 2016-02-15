package com.elster.jupiter.metering.imports.impl.usagepoint.exceptions;

import com.elster.jupiter.util.exception.MessageSeed;

public class FileImportParserException extends ImportException {

    public FileImportParserException(MessageSeed message, Object... args) {
        super(message, args);
    }
}