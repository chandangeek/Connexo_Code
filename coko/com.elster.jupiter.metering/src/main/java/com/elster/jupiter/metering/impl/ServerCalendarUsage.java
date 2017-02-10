/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.UsagePoint;

import com.google.common.collect.Range;

import java.time.Instant;

/**
 * Adds behavior to {@link com.elster.jupiter.metering.UsagePoint.CalendarUsage}
 * that is specific to server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-02-09 (13:09)
 */
public interface ServerCalendarUsage extends UsagePoint.CalendarUsage {
    boolean overlaps(Range<Instant> period);
}