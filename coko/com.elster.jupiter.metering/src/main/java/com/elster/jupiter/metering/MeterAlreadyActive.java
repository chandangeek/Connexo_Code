package com.elster.jupiter.metering;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.time.DefaultDateTimeFormatters;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class MeterAlreadyActive extends LocalizedException {

    public MeterAlreadyActive(Thesaurus thesaurus, Meter meter, Instant instant) {
        super(thesaurus, MessageSeeds.METER_ALREADY_ACTIVE, meter.getName(), formatted(instant, thesaurus));
    }

    private static String formatted(Instant instant, Thesaurus thesaurus) {
        DateTimeFormatter dateTimeFormatter = DefaultDateTimeFormatters.mediumDate().withLongTime().build();
        return thesaurus.forLocale(dateTimeFormatter).withZone(ZoneId.of("UTC")).format(instant);
    }
}
