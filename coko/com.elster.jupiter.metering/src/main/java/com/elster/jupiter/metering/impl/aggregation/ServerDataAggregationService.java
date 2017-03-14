/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.metering.aggregation.DataAggregationService;
import com.elster.jupiter.metering.impl.ServerUsagePoint;

import com.google.common.collect.Range;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

public interface ServerDataAggregationService extends DataAggregationService {

    List<MeterActivationSet> getMeterActivationSets(ServerUsagePoint usagePoint, Range<Instant> period);

    List<MeterActivationSet> getMeterActivationSets(ServerUsagePoint usagePoint, Instant when);

    Category getTimeOfUseCategory();

    List<DetailedCalendarUsage> introspect(ServerUsagePoint usagePoint, Instant instant);

    interface DetailedCalendarUsage {
        Optional<Calendar> getCalendar();
        IntervalLength getIntervalLength();
        ZoneId getZoneId();
    }

}