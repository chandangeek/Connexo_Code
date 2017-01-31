/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar;

import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.List;

/**
 * Models a type of day on which {@link Event}s will occur.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-04-07 (12:11)
 */
@ProviderType
public interface DayType extends HasId, HasName {

    List<EventOccurrence> getEventOccurrences();

    Calendar getCalendar();

    long getVersion();

    Instant getCreateTime();

    Instant getModTime();

    String getUserName();

}