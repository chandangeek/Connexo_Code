/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.metering.aggregation.DataAggregationService;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.impl.ServerUsagePoint;
import com.elster.jupiter.nls.Thesaurus;

import com.google.common.collect.Range;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

public interface ServerDataAggregationService extends DataAggregationService {

    Clock getClock();

    Thesaurus getThesaurus();

    Category getTimeOfUseCategory();

    /**
     * Tests if the specified {@link EffectiveMetrologyConfigurationOnUsagePoint}
     * contains the specified {@link MetrologyContract}.
     *
     * @param mistery The subject of this test
     * @param contract The MetrologyContract
     * @return <code>true</code> iff the EffectiveMetrologyConfigurationOnUsagePoint contains the MetrologyContract
     */
    boolean hasContract(EffectiveMetrologyConfigurationOnUsagePoint mistery, MetrologyContract contract);

    List<MeterActivationSet> getMeterActivationSets(ServerUsagePoint usagePoint, Range<Instant> period);

    List<MeterActivationSet> getMeterActivationSets(ServerUsagePoint usagePoint, Instant when);

    List<DetailedCalendarUsage> introspect(ServerUsagePoint usagePoint, Instant instant);

    interface DetailedCalendarUsage {
        Optional<Calendar> getCalendar();
        IntervalLength getIntervalLength();
        ZoneId getZoneId();
    }

}