package com.elster.jupiter.calendar.impl.importers;

import com.elster.jupiter.calendar.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

class YearRequiredForNotRecurringTransitions extends LocalizedException {
    YearRequiredForNotRecurringTransitions(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.YEAR_REQUIRED_FOR_NOT_RECURRING_TRANSITIONS);
    }
}
