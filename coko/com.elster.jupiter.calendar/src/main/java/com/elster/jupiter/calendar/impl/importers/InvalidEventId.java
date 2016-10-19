package com.elster.jupiter.calendar.impl.importers;

import com.elster.jupiter.calendar.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

class InvalidEventId extends LocalizedException {

    InvalidEventId(Thesaurus thesaurus, Object id) {
        super(thesaurus, MessageSeeds.INVALID_EVENT_ID, id);
    }
}
