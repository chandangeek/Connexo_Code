/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.AllowedCalendar;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.MessageSeeds;

/**
 * Models the exceptional situation that occurs when a {@link AllowedCalendar}
 * is being marked obsolete while it is still being used by one or more
 * {@link Device s.)
 */
public class VetoObsoleteAllowedCalendarException  extends LocalizedException {
    public VetoObsoleteAllowedCalendarException(Thesaurus thesaurus, AllowedCalendar allowedCalendar) {
        super(thesaurus, MessageSeeds.VETO_ALLOWED_CALENDAR_OBSOLETE, allowedCalendar.getName());
        this.set("allowedCalendarName", allowedCalendar.getName());
    }
}
