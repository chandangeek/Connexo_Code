/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl.importers;

import com.elster.jupiter.calendar.impl.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

class YearRequiredForNotRecurringTransitions extends LocalizedException {
    YearRequiredForNotRecurringTransitions(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.YEAR_REQUIRED_FOR_NOT_RECURRING_TRANSITIONS);
    }
}
