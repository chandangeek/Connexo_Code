/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

import java.time.LocalDate;

class CannotAddPastExceptionsToActiveCalendar extends LocalizedException {

    public CannotAddPastExceptionsToActiveCalendar(Thesaurus thesaurus, String dayTypeName, LocalDate date) {
        super(thesaurus, MessageSeeds.CANNOT_ADD_PAST_EXCEPTIONS_TO_ACTIVE_CALENDAR, dayTypeName, date);
    }
}
