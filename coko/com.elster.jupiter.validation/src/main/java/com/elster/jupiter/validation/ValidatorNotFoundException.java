package com.elster.jupiter.validation;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.validation.impl.MessageSeeds;

public class ValidatorNotFoundException extends LocalizedException {

    public ValidatorNotFoundException(Thesaurus thesaurus, String implementation) {
        super(thesaurus, MessageSeeds.NO_SUCH_VALIDATOR, implementation);
        set("implementation", implementation);
    }
}
