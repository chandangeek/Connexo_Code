/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.impl.MessageSeeds;

public class VetoCalendarDeleteException extends LocalizedException {

    public VetoCalendarDeleteException(Thesaurus thesaurus, Calendar calendar) {
        super(thesaurus, MessageSeeds.VETO_CALENDAR_DELETION, calendar.getName());
    }
}
