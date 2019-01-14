/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.wsdl.impl;

import com.google.common.base.Strings;

import javax.xml.bind.DatatypeConverter;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class XsdDateTimeConverter {

    public static Instant unmarshalDateTime(String dateTime) {
        return Strings.isNullOrEmpty(dateTime) ? null : DatatypeConverter.parseDate(dateTime).toInstant();
    }

    public static LocalTime unmarshalTime(String time) {
        return Strings.isNullOrEmpty(time) ? null : ZonedDateTime.ofInstant(DatatypeConverter.parseTime(time).toInstant(), ZoneId.systemDefault()).toLocalTime();
    }

    public static String marshalDate(Instant date) {
        return DatatypeConverter.printDate(asCalendar(date));
    }

    public static String marshalTime(LocalTime time) {
        return DatatypeConverter.printTime(asCalendar(time.atDate(LocalDate.ofEpochDay(1))));
    }

    public static String marshalDateTime(Instant dateTime) {
        return DatatypeConverter.printDateTime(asCalendar(dateTime));
    }

    private static Calendar asCalendar(Instant instant) {
        return GregorianCalendar.from(ZonedDateTime.ofInstant(instant, ZoneId.systemDefault()));
    }

    private static Calendar asCalendar(LocalDateTime localDateTime) {
        return GregorianCalendar.from(localDateTime.atZone(ZoneId.systemDefault()));
    }
}
