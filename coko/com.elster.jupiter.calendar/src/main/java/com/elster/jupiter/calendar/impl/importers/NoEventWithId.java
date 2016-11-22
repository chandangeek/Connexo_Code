package com.elster.jupiter.calendar.impl.importers;

import com.elster.jupiter.calendar.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

class NoEventWithId extends LocalizedException {
    NoEventWithId(Thesaurus thesaurus, String eventId) {
        super(thesaurus, MessageSeeds.NO_EVENT_DEFINED_WITH_ID, eventId);
    }
}
