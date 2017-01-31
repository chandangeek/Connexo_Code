/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.parsers;

import com.energyict.mdc.device.data.importers.impl.exceptions.ValueParserException;
import com.energyict.mdc.device.data.importers.impl.properties.TimeZonePropertySpec;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static com.elster.jupiter.util.Checks.is;

public class DateParser implements FieldParser<ZonedDateTime> {

    private final String format;
    private final String timeZone;

    public DateParser(String format, String timeZone) {
        this.format = format;
        this.timeZone = timeZone;
    }

    public ZonedDateTime parse(String value) throws ValueParserException {
        if (is(value).emptyOrOnlyWhiteSpace()) {
            return null;
        }
        return parseNonEmptyDateString(value);
    }

    private ZonedDateTime parseNonEmptyDateString(String value) throws ValueParserException {
        try {
            DateTimeFormatter dataTimeFormatter = DateTimeFormatter.ofPattern(format);
            LocalDateTime localDateTime = LocalDateTime.parse(value, dataTimeFormatter);
            ZoneId zoneId = ZoneId.from(TimeZonePropertySpec.format.parse(timeZone));
            return ZonedDateTime.of(localDateTime, zoneId);
        } catch (Exception e) {
            throw new ValueParserException(value, format);
        }
    }
}