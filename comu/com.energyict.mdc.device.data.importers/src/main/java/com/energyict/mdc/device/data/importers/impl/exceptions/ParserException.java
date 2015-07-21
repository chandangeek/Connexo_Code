package com.energyict.mdc.device.data.importers.impl.exceptions;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;

public class ParserException extends ImportException {
    public enum Type {
        INVALID_DATE_FORMAT(MessageSeeds.DATE_FORMAT_IS_NOT_VALID),
        LINE_FORMAT(MessageSeeds.LINE_FORMAT_ERROR),
        ;

        Type(MessageSeed messageSeed) {
            this.message = messageSeed;
        }

        private MessageSeed message;

        public ParserException create(Object... args) throws ParserException {
            throw new ParserException(this.message, args);
        }
    }

    public ParserException(MessageSeed message, Object... args) {
        super(message, args);
    }
}