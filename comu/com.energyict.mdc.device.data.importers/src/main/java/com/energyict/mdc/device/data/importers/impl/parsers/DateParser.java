package com.energyict.mdc.device.data.importers.impl.parsers;


import com.elster.jupiter.util.Checks;
import com.energyict.mdc.device.data.importers.impl.exceptions.ParserException;
import com.energyict.mdc.device.data.importers.impl.properties.TimeZonePropertySpec;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateParser implements FieldParser<ZonedDateTime> {

    private final String format;
    private final String timeZone;

    public DateParser(String format, String timeZone) {
        this.format = format;
        this.timeZone = timeZone;
    }

    public ZonedDateTime parse(String value) throws ParserException {
        if (Checks.is(value).emptyOrOnlyWhiteSpace()) {
            return null;
        }
        return parseNonEmptyDateString(value);
    }

    private ZonedDateTime parseNonEmptyDateString(String value) throws ParserException {
        try {
            DateTimeFormatter dataTimeFormatter = DateTimeFormatter.ofPattern(format);
            LocalDateTime localDateTime = LocalDateTime.parse(value, dataTimeFormatter);
            ZoneId zoneId = ZoneId.from(TimeZonePropertySpec.format.parse(timeZone));
            return ZonedDateTime.of(localDateTime, zoneId);
        } catch (Exception e) {
            throw new ParserException(ParserException.Type.INVALID_DATE_FORMAT, format, value);
        }
    }
}