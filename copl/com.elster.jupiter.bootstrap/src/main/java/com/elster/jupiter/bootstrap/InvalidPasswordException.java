package com.elster.jupiter.bootstrap;

import com.elster.jupiter.util.exception.BaseException;

public class InvalidPasswordException extends BaseException {

    public InvalidPasswordException() {
        super(MessageSeeds.JDBC_PASSWORD_NOT_CORRECT);
    }
}
