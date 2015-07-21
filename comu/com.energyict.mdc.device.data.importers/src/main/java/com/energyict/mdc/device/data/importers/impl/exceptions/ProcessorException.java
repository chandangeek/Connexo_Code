package com.energyict.mdc.device.data.importers.impl.exceptions;

import com.elster.jupiter.util.exception.MessageSeed;

public class ProcessorException extends ImportException {

    protected ProcessorException(MessageSeed message, Object... args) {
        super(message, args);
    }
}