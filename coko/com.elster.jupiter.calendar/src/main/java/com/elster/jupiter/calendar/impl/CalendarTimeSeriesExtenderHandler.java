/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.elster.jupiter.util.streams.Currying.perform;


/**
 * Extends the TimeSeries of one or more {@link com.elster.jupiter.calendar.Calendar}s
 * with one additional year according to the specs of the Calendar.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-02-24 (10:22)
 */
public class CalendarTimeSeriesExtenderHandler implements TaskExecutor {
    // Payload that indicates that all calendars must be recalculated
    static final String GLOBAL_START_PAYLOAD = "GLOBAL";
    static final Pattern SINGLE_TIMESERIES_PAYLOAD_PATTERN = Pattern.compile("(\\d*)#(\\d*)");

    private final ServerCalendarService calendarService;
    private final MessageService messageService;

    public CalendarTimeSeriesExtenderHandler(ServerCalendarService calendarService, MessageService messageService) {
        this.calendarService = calendarService;
        this.messageService = messageService;
    }

    @Override
    public void execute(TaskOccurrence occurrence) {
        if (GLOBAL_START_PAYLOAD.equals(occurrence.getPayLoad())) {
            this.extendAllExistingCalendarTimeSeries();
        } else {
            Matcher matcher = SINGLE_TIMESERIES_PAYLOAD_PATTERN.matcher(occurrence.getPayLoad());
            if (matcher.matches()) {
                long calendarId = Long.parseLong(matcher.group(1));
                long timeSeriesId = Long.parseLong(matcher.group(2));
                this.calendarService
                        .findCalendar(calendarId)
                        .map(ServerCalendar.class::cast)
                        .ifPresent(calendar -> calendar.extend(timeSeriesId));
            } else {
                throw new IllegalArgumentException("Unexpected payload: " + occurrence.getPayLoad());
            }
        }
    }

    private void extendAllExistingCalendarTimeSeries() {
        DestinationSpec destinationSpec = this.messageService.getDestinationSpec(CalendarTimeSeriesExtenderHandlerFactory.TASK_DESTINATION).get();
        List<ServerCalendar> calendars = this.findAllCalendars();
        calendars.forEach(perform(this::publishMessagesAndBumpEndYear).with(destinationSpec));
    }

    private List<ServerCalendar> findAllCalendars() {
        return this.calendarService.findAllCalendarsForExtension();
    }

    private void publishMessagesAndBumpEndYear(ServerCalendar calendar, DestinationSpec destinationSpec) {
        calendar
            .getCachedTimeSeries()
            .forEach(perform(this::publishMessage).with(destinationSpec));
        calendar.bumpEndYear();
    }

    private void publishMessage(CalendarTimeSeriesEntity cachedTimeSeries, DestinationSpec destinationSpec) {
        destinationSpec.message(this.payLoadFor(cachedTimeSeries)).send();
    }

    private String payLoadFor(CalendarTimeSeriesEntity cachedTimeSeries) {
        return cachedTimeSeries.calendar().getId() + "#" + cachedTimeSeries.timeSeries().getId();
    }

}