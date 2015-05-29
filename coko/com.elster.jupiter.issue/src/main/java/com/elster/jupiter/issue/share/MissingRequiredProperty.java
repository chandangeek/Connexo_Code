package com.elster.jupiter.issue.share;

import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class MissingRequiredProperty extends LocalizedException {

    private static final long serialVersionUID = 1L;

    public MissingRequiredProperty(Thesaurus thesaurus, String missingKey) {
        super(thesaurus, MessageSeeds.PROPERTY_MISSING, missingKey);
        set("missingKey", missingKey);
    }
}