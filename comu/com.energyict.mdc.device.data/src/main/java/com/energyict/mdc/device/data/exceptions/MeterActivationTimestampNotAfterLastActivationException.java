package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class MeterActivationTimestampNotAfterLastActivationException extends LocalizedException {

    public MeterActivationTimestampNotAfterLastActivationException(Thesaurus thesaurus, DateTimeFormatter formatter, Instant activationTimestamp, Instant lastActivationTimestamp) {
        super(thesaurus, MessageSeeds.METER_ACTIVATION_TIMESTAMP_NOT_AFTER_LAST_ACTIVATION,
                getFormattedInstant(formatter, activationTimestamp),
                getFormattedInstant(formatter, lastActivationTimestamp));
    }

    private static String getFormattedInstant(DateTimeFormatter formatter, Instant time) {
        return formatter.format(LocalDateTime.ofInstant(time, ZoneId.systemDefault()));
    }
}
