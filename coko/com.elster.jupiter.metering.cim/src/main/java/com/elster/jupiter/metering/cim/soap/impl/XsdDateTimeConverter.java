package com.elster.jupiter.metering.cim.soap.impl;

import javax.xml.bind.DatatypeConverter;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class XsdDateTimeConverter {

    public static Date unmarshal(String dateTime) {
        return DatatypeConverter.parseDate(dateTime).getTime();
    }

    public static String marshalDate(Date date) {
        final GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        calendar.setTime(date);
        return DatatypeConverter.printDate(calendar);
    }

    public static String marshalDateTime(Date dateTime) {
        final GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(dateTime);
        return DatatypeConverter.printDateTime(calendar);
    }

}