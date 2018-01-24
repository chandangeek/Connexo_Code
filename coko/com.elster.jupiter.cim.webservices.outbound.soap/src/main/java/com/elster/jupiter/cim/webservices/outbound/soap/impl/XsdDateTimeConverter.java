/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.outbound.soap.impl;

import com.google.common.base.Strings;

import javax.xml.bind.DatatypeConverter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class XsdDateTimeConverter {

    public static Instant unmarshal(String dateTime) {
        if (Strings.isNullOrEmpty(dateTime)) {
            return null;
        } else {
            return DatatypeConverter.parseDate(dateTime).toInstant();
        }
    }

    public static String marshalDate(Instant date) {
        return DatatypeConverter.printDate(asCalendar(date));
    }

    public static String marshalDateTime(Instant dateTime) {
        return DatatypeConverter.printDateTime(asCalendar(dateTime));
    }

    private static Calendar asCalendar(Instant instant) {
        return GregorianCalendar.from(ZonedDateTime.ofInstant(instant, ZoneId.systemDefault()));
    }
}