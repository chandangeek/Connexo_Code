/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.imports.impl.parsers;


import com.elster.jupiter.fileimport.csvimport.FieldParser;
import com.elster.jupiter.fileimport.csvimport.exceptions.ValueParserException;
import com.elster.jupiter.metering.imports.impl.properties.TimeZonePropertySpec;
import com.elster.jupiter.util.Checks;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class InstantParser implements FieldParser<Instant> {

    private final String format;
    private final String timeZone;

    public InstantParser(String format, String timeZone) {
        this.format = format;
        this.timeZone = timeZone;
    }

    @Override
    public Class<Instant> getValueType() {
        return Instant.class;
    }

    public Instant parse(String value) throws ValueParserException {
        if (Checks.is(value).emptyOrOnlyWhiteSpace()) {
            return null;
        }
        return parseNonEmptyDateString(value);
    }

    private Instant parseNonEmptyDateString(String value) throws ValueParserException {
        try {
            if (value.equalsIgnoreCase("NaN")) {
                return Instant.EPOCH;
            }

            DateTimeFormatter dataTimeFormatter = DateTimeFormatter.ofPattern(format);
            LocalDateTime localDateTime = LocalDateTime.parse(value, dataTimeFormatter);
            ZoneId zoneId = ZoneId.from(TimeZonePropertySpec.format.parse(timeZone));
            return ZonedDateTime.of(localDateTime, zoneId).toInstant();
        } catch (Exception e) {
            throw new ValueParserException(value, format);
        }
    }
}