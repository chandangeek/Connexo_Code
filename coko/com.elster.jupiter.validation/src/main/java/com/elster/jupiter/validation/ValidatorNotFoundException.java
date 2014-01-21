package com.elster.jupiter.validation;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.BaseException;

public class ValidatorNotFoundException extends BaseException {

    public ValidatorNotFoundException(Thesaurus thesaurus, String implementation) {
        super(ExceptionTypes.NO_SUCH_VALIDATOR, buildMessage(thesaurus, implementation));
        set("implementation", implementation);
    }

    private static String buildMessage(Thesaurus thesaurus, String implementation) {
        return thesaurus.getFormat(MessageSeeds.NO_SUCH_VALIDATOR).format(implementation);
    }
}
