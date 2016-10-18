package com.elster.jupiter.calendar;

import com.elster.jupiter.nls.Thesaurus;

public enum Status {
    ACTIVE, INACTIVE;

    public String getDisplayName(Thesaurus thesaurus) {
        return CalendarStatusTranslationKeys.translationFor(this, thesaurus);
    }
}
