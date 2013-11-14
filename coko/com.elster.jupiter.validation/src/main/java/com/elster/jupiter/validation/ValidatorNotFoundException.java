package com.elster.jupiter.validation;

import com.elster.jupiter.util.exception.BaseException;

public class ValidatorNotFoundException extends BaseException {

    public ValidatorNotFoundException(String validator) {
        super(ExceptionTypes.NO_SUCH_VALIDATOR, "Validator {0} does not exist.");
        set("validator", validator);
    }
}
