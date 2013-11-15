package com.elster.jupiter.validation;

import com.elster.jupiter.util.exception.BaseException;

public class ValidatorNotFoundException extends BaseException {

    public ValidatorNotFoundException(String implementation) {
        super(ExceptionTypes.NO_SUCH_VALIDATOR, "Validator {0} does not exist.");
        set("implementation", implementation);
    }
}
