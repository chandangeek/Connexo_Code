package com.elster.jupiter.bootstrap;

import com.elster.jupiter.util.exception.BaseException;

public class InvalidPasswordException extends BaseException {
    private static final long serialVersionUID = 1L;

    public InvalidPasswordException() {
        super(MessageSeeds.JDBC_PASSWORD_NOT_CORRECT);
    }
}
