package com.elster.jupiter.util.exception;

public class SqlInjectionException extends PersistenceException {

    public SqlInjectionException(MessageSeed messageSeed, String field) {
        super(messageSeed, field);
    }
}
