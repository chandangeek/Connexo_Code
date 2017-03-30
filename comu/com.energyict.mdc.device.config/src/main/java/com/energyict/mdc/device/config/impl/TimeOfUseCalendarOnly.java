/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

class TimeOfUseCalendarOnly extends LocalizedException {

    public TimeOfUseCalendarOnly(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.TIME_OF_USE_CALENDAR_ONLY);
    }
}
