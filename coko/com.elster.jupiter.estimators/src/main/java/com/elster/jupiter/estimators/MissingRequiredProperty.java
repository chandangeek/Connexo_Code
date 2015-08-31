package com.elster.jupiter.estimators;

import com.elster.jupiter.estimators.impl.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class MissingRequiredProperty extends LocalizedException {

    public MissingRequiredProperty(Thesaurus thesaurus, String missingKey) {
        super(thesaurus, MessageSeeds.MISSING_PROPERTY, missingKey);
        set("missingKey", missingKey);
    }
}
