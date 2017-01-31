/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.cim.soap.impl;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.xml.bind.DatatypeConverter;
import java.util.Date;

public class XsdDateTimeConverter {

    public static Date unmarshal(String dateTime) {
        return DatatypeConverter.parseDate(dateTime).getTime();
    }

    public static String marshalDate(Date date) {
        return DatatypeConverter.printDate(new DateMidnight(date, DateTimeZone.UTC).toGregorianCalendar());
    }

    public static String marshalDateTime(Date dateTime) {
        return DatatypeConverter.printDateTime(new DateTime(dateTime, DateTimeZone.UTC).toGregorianCalendar());
    }

}