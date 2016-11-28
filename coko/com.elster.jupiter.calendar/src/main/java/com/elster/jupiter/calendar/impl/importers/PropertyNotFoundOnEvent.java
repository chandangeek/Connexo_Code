package com.elster.jupiter.calendar.impl.importers;

import com.elster.jupiter.calendar.impl.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

class PropertyNotFoundOnEvent extends LocalizedException {
    PropertyNotFoundOnEvent(Thesaurus thesaurus, String name) {
        super(thesaurus, MessageSeeds.PROPERTY_NOT_FOUND_ON_EVENT, name);
    }
}
