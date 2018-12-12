/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.csvimport.exceptions;

import com.elster.jupiter.util.exception.MessageSeed;

public class FileImportParserException extends ImportException {

    public FileImportParserException(MessageSeed message, Object... args) {
        super(message, args);
    }
}