package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

class NoSuchDayType extends LocalizedException {
    public NoSuchDayType(Thesaurus thesaurus, String dayTypeName) {
        super(thesaurus, MessageSeeds.NO_DAYTYPE_DEFINED_WITH_NAME, dayTypeName);
    }
}
