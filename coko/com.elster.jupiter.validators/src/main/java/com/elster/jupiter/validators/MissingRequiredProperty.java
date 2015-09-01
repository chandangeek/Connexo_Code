package com.elster.jupiter.validators;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.validators.impl.MessageSeeds;

public class MissingRequiredProperty extends LocalizedException {

    public MissingRequiredProperty(Thesaurus thesaurus, String missingKey) {
        super(thesaurus, MessageSeeds.MISSING_PROPERTY, missingKey);
        set("missingKey", missingKey);
    }
}
