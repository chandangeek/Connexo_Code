/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar;

import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ProviderType;

import java.time.DayOfWeek;
import java.time.Instant;

/**
 * Defines the standard occurrences of {@link Event}s
 * in a period of time by specifying the {@link DayType}s
 * for each of the human Calendar weekdays.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-04-07 (12:19)
 */
@ProviderType
public interface Period extends HasId, HasName {
    DayType getDayType(DayOfWeek dayOfWeek);

    Calendar getCalendar();

    Instant getCreateTime();

    long getVersion();

    Instant getModTime();

    String getUserName();
}