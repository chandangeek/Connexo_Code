package com.elster.jupiter.estimation;

import com.elster.jupiter.estimation.impl.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Created by igh on 3/03/2015.
 */
public class MissingRequiredProperty extends LocalizedException {

    public MissingRequiredProperty(Thesaurus thesaurus, String missingKey) {
        super(thesaurus, MessageSeeds.MISSING_PROPERTY, missingKey);
        set("missingKey", missingKey);
    }
}