package com.elster.jupiter.calendar.impl.importers;

import com.elster.jupiter.calendar.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

class NoDayTypeForId extends LocalizedException {
    NoDayTypeForId(Thesaurus thesaurus, int id) {
        super(thesaurus, MessageSeeds.NO_DAYTYPE_DEFINED_WITH_ID, id);
    }
}
