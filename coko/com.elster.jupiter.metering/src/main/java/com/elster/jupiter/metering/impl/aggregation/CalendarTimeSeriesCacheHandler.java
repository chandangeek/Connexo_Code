/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.impl.ServerUsagePoint;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;

import java.time.Instant;

/**
 * Triggered when a {@link com.elster.jupiter.calendar.Calendar}
 * or a {@link MetrologyConfiguration} is linked to a {@link UsagePoint}
 * and will cache the time series of all Calendars from the link point in time onwards.
 * From the link point in time onwards, the {@link ServerDataAggregationService}
 * will determine which Calendars and which ZoneId that need to be used
 * against which {@link IntervalLength}. The related time series
 * is then obtained and cached so that is ready and available to be
 * used by the DataAggregationService when triggered on the UsagePoint.
 * This avoids that triggering requires a transaction and that
 * the calculation takes a lot of time simply because a time series
 * needed to be generated from the Calendar first.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-03-13 (13:32)
 */
public class CalendarTimeSeriesCacheHandler implements MessageHandler {

    static final String USAGE_POINT_TIMESTAMP_SEPARATOR = "#";

    private final MeteringService meteringService;
    private final ServerDataAggregationService dataAggregationService;

    CalendarTimeSeriesCacheHandler(MeteringService meteringService, ServerDataAggregationService dataAggregationService) {
        this.meteringService = meteringService;
        this.dataAggregationService = dataAggregationService;
    }

    public static String payloadFor(UsagePoint usagePoint, Instant timestamp) {
        return Long.toString(usagePoint.getId()) + USAGE_POINT_TIMESTAMP_SEPARATOR + Long.toString(timestamp.toEpochMilli());
    }

    @Override
    public void process(Message message) {
        String[] usagePointIdAndUtcTimestamp = new String(message.getPayload()).split(USAGE_POINT_TIMESTAMP_SEPARATOR);
        long usagePointId = Long.parseLong(usagePointIdAndUtcTimestamp[0]);
        long utcTimestamp = Long.parseLong(usagePointIdAndUtcTimestamp[1]);
        Instant instant = Instant.ofEpochMilli(utcTimestamp);
        this.meteringService
                .findUsagePointById(usagePointId)
                .map(ServerUsagePoint.class::cast)
                .map(usagePoint -> this.execute(usagePoint, instant))
                .orElseThrow(() -> new IllegalArgumentException("Usagepoint with ID " + usagePointId + " not found"));
    }

    private ServerUsagePoint execute(ServerUsagePoint usagePoint, Instant instant) {
        this.dataAggregationService
                .introspect(usagePoint, instant)
                .forEach(this::execute);
        return usagePoint;
    }

    private void execute(ServerDataAggregationService.DetailedCalendarUsage calendarUsage) {
        calendarUsage.getCalendar().toTimeSeries(calendarUsage.getIntervalLength().toTemporalAmount(), calendarUsage.getZoneId());
    }

}