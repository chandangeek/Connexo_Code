/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.imports.impl.exceptions;

import com.elster.jupiter.util.exception.MessageSeed;

public class FileImportParserException extends ImportException {

    public FileImportParserException(MessageSeed message, Object... args) {
        super(message, args);
    }
}