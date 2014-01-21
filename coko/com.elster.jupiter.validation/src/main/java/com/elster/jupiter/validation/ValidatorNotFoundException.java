package com.elster.jupiter.validation;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class ValidatorNotFoundException extends LocalizedException {

    public ValidatorNotFoundException(Thesaurus thesaurus, String implementation) {
        super(thesaurus, MessageSeeds.NO_SUCH_VALIDATOR, thesaurus, implementation);
        set("implementation", implementation);
    }
}
