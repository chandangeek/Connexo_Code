/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.UsagePoint;

import java.util.List;

/**
 * Adds behavior to {@link UsagePoint} that is specific to server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-02-09 (13:08)
 */
public interface ServerUsagePoint extends UsagePoint {
    void add(ServerCalendarUsage calendarUsage);
    List<ServerCalendarUsage> getCalendarUsages();
    List<ServerCalendarUsage> getTimeOfUseCalendarUsages();
}