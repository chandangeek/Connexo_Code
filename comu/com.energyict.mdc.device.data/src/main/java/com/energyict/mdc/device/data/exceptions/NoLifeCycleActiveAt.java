/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.time.DefaultDateTimeFormatters;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class NoLifeCycleActiveAt extends LocalizedException {

    public NoLifeCycleActiveAt(Thesaurus thesaurus, MessageSeed messageSeed, Instant shipmentDate, Instant maxPast, Instant maxFuture) {
        super(thesaurus, messageSeed, formatted(shipmentDate, thesaurus), formatted(maxPast, thesaurus), formatted(maxFuture, thesaurus));
        set("shipmentDate", shipmentDate);
        set("maxPast", maxPast);
        set("maxFuture", maxFuture);
    }

    public static String formatted(Instant instant, Thesaurus thesaurus) {
        DateTimeFormatter dateTimeFormatter = DefaultDateTimeFormatters.mediumDate().withLongTime().build();
        return thesaurus.forLocale(dateTimeFormatter).withZone(ZoneId.systemDefault()).format(instant);
    }
}
