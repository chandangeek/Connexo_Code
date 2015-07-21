package com.energyict.mdc.device.data.importers.impl.exceptions;

import com.elster.jupiter.util.exception.MessageSeed;

public class ValueParserException extends ImportException {

    public ValueParserException(MessageSeed message, Object... args) {
        super(message, args);
    }
}
