package com.elster.jupiter.calendar.impl.importers;

import com.elster.jupiter.calendar.impl.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

class InvalidEventCode extends LocalizedException {

    InvalidEventCode(Thesaurus thesaurus, Object code) {
        super(thesaurus, MessageSeeds.INVALID_EVENT_CODE, code);
    }
}
