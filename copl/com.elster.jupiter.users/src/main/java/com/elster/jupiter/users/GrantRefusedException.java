package com.elster.jupiter.users;


import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.util.exception.MessageSeed;

public class GrantRefusedException extends LocalizedFieldValidationException {
    public GrantRefusedException(MessageSeed messageSeed, String javaFieldName) {
        super(messageSeed, javaFieldName);
    }

    public GrantRefusedException(MessageSeed messageSeed, String javaFieldName, Object... args) {
        super(messageSeed, javaFieldName, args);
    }
}
