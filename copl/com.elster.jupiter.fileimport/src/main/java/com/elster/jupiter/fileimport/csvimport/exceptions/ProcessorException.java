/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.csvimport.exceptions;

import com.elster.jupiter.util.exception.MessageSeed;

public class ProcessorException extends ImportException {
    private boolean stopImport = false;

    public ProcessorException(MessageSeed message, Object... args) {
        super(message, args);
    }

    public ProcessorException andStopImport() {
        stopImport = true;
        return this;
    }

    public boolean shouldStopImport() {
        return stopImport;
    }
}
