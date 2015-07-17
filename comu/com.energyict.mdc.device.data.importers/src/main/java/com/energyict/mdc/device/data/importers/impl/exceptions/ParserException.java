package com.energyict.mdc.device.data.importers.impl.exceptions;

public class ParserException extends ImportException {
    public static enum Type {
        FAILED_TO_READ_FROM_FILE("DVI.failedToReadFile"),
        INVALID_DATE_FORMAT("DVI.invalidDateFormat"),
        WRONG_NUMBER_OF_FIELDS("DVI.invalidNumberOfFields");

        private Type(String message){
            this.message = message;
        }

        private String message;
    };

    Type type;

    public ParserException(Type type) {
        this.type = type;
    }

    public ParserException(Type type, String expected, String actual) {
        this.type = type;
        this.expected = expected;
        this.actual = actual;
    }

    public String getMessage() {
        return this.type.message;
    }
}