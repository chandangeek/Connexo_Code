/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.wsdl.impl;

import com.google.common.base.Strings;

import javax.xml.bind.DatatypeConverter;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static java.time.ZoneOffset.UTC;

public class XsdDateTimeConverter {

    public static Instant unmarshalDateTime(String dateTime) {
        return Strings.isNullOrEmpty(dateTime) ? null : DatatypeConverter.parseDate(dateTime).toInstant();
    }

    public static LocalTime unmarshalTime(String time) {
        return Strings.isNullOrEmpty(time) ? null : ZonedDateTime.ofInstant(DatatypeConverter.parseTime(time).toInstant(), ZoneId.systemDefault()).toLocalTime();
    }

    public static String marshalDate(Instant date) {
        return DateTimeFormatter.ISO_LOCAL_DATE.format(ZonedDateTime.ofInstant(date, ZoneId.of("UTC")));
    }

    public static String marshalTime(LocalTime time) {
        return DateTimeFormatter.ISO_LOCAL_TIME.format(time.atOffset(UTC));
    }

    public static String marshalDateTime(Instant dateTime) {
        return DatatypeConverter.printDateTime(asCalendarUTC(dateTime));
    }

    private static Calendar asCalendarUTC(Instant instant) {
        return GregorianCalendar.from(ZonedDateTime.ofInstant(instant, ZoneId.of("UTC")));
    }
}
