package com.energyict.mdc.device.data.importers.impl.exceptions;

import com.elster.jupiter.util.exception.MessageSeed;

public class FileImportParserException extends ImportException {

    public FileImportParserException(MessageSeed message, Object... args) {
        super(message, args);
    }
}