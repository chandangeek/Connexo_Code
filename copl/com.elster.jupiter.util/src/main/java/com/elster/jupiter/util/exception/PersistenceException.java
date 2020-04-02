package com.elster.jupiter.util.exception;

public abstract class PersistenceException extends BaseException {

    private static final long serialVersionUID = 1;

    protected PersistenceException(MessageSeed messageSeed) {
        super(messageSeed);
    }

    protected PersistenceException(MessageSeed messageSeed, Object... args) {
        super(messageSeed, args);
    }

    protected PersistenceException(MessageSeed messageSeed, Throwable cause) {
        super(messageSeed, cause);
    }

    protected PersistenceException(MessageSeed messageSeed, Throwable cause, Object... args) {
        super(messageSeed, cause, args);
    }
}
