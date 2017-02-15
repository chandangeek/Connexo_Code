package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.Channel;

import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;

public class ReadingWithValidationStatusFactory {

    private final Clock clock;

    private final ReadingWithValidationStatus.ChannelGeneralValidation channelGeneralValidation;
    private final Channel channel;

    public ReadingWithValidationStatusFactory(Clock clock, Channel channel, boolean isValidationActive, Instant channelLastChecked) {
        this.clock = clock;
        this.channelGeneralValidation = new ReadingWithValidationStatus.ChannelGeneralValidation(isValidationActive, channelLastChecked);
        this.channel = channel;
    }

    public ChannelReadingWithValidationStatus createChannelReading(Instant readingTimeStamp) {
        return new ChannelReadingWithValidationStatus(this.channel, ZonedDateTime.ofInstant(readingTimeStamp, clock.getZone()), this.channelGeneralValidation);
    }

    public RegisterReadingWithValidationStatus createRegisterReading(Instant readingTimeStamp) {
        return new RegisterReadingWithValidationStatus(ZonedDateTime.ofInstant(readingTimeStamp, clock.getZone()), this.channelGeneralValidation);
    }
}
