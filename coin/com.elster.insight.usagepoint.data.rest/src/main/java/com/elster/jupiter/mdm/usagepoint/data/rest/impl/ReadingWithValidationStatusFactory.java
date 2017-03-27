package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.Category;
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
    private final UsagePoint.UsedCalendars calendars;
    private final Category timeOfUseCategory;

    public ReadingWithValidationStatusFactory(Clock clock, Channel channel, boolean isValidationActive, Instant channelLastChecked, UsagePoint usagePoint, CalendarService calendarService) {
        this.clock = clock;
        this.timeOfUseCategory = calendarService.findCategoryByName(OutOfTheBoxCategory.TOU.name()).get();
        this.channelGeneralValidation = new ReadingWithValidationStatus.ChannelGeneralValidation(isValidationActive, channelLastChecked);
        this.channel = channel;
        this.calendars = usagePoint.getUsedCalendars();
    }

    public ChannelReadingWithValidationStatus createChannelReading(Instant readingTimeStamp) {
        return new ChannelReadingWithValidationStatus(
                this.channel,
                ZonedDateTime.ofInstant(readingTimeStamp, this.clock.getZone()),
                this.channelGeneralValidation,
                this.calendars.getCalendar(readingTimeStamp, this.timeOfUseCategory));
    }

    public RegisterReadingWithValidationStatus createRegisterReading(Instant readingTimeStamp) {
        return new RegisterReadingWithValidationStatus(ZonedDateTime.ofInstant(readingTimeStamp, clock.getZone()), this.channelGeneralValidation);
    }

}