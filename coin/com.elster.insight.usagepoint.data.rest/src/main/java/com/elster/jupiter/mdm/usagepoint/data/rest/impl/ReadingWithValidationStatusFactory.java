package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.OutOfTheBoxCategory;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.UsagePoint;

import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;

public class ReadingWithValidationStatusFactory {

    private final Clock clock;
    private final ReadingWithValidationStatus.ChannelGeneralValidation channelGeneralValidation;

    private final Channel channel;
    private final UsagePoint usagePoint;
    private final CalendarService calendarService;

    public ReadingWithValidationStatusFactory(Clock clock, Channel channel, boolean isValidationActive, Instant channelLastChecked, UsagePoint usagePoint, CalendarService calendarService) {
        this.clock = clock;
        this.calendarService = calendarService;
        this.channelGeneralValidation = new ReadingWithValidationStatus.ChannelGeneralValidation(isValidationActive, channelLastChecked);
        this.channel = channel;
        this.usagePoint = usagePoint;
    }

    public ChannelReadingWithValidationStatus createChannelReading(Instant readingTimeStamp) {
        return new ChannelReadingWithValidationStatus(this.channel,
                ZonedDateTime.ofInstant(readingTimeStamp, clock.getZone()),
                this.channelGeneralValidation,
                usagePoint.getUsedCalendars().getCalendar(readingTimeStamp, calendarService.findCategoryByName(OutOfTheBoxCategory.TOU.name()).get()));
    }

    public RegisterReadingWithValidationStatus createRegisterReading(Instant readingTimeStamp) {
        return new RegisterReadingWithValidationStatus(ZonedDateTime.ofInstant(readingTimeStamp, clock.getZone()), this.channelGeneralValidation);
    }
}
