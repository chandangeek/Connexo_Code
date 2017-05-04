/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class ReferenceDate extends AbstractField<ReferenceDate> {

    private byte[] date;
    private static final int LENGTH = 3;

    public byte[] getBytes() {
        return date;
    }

    public byte[] getDate() {
        return date;
    }

    public ReferenceDate parse(byte[] rawData, int offset) throws CTRParsingException {
        date = ProtocolTools.getSubArray(rawData, offset, offset + getLength());
        return this;
    }

    public ReferenceDate parse(Date date, TimeZone timeZone) {
        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
        }
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setLenient(true);     //overflow in days causes month + 1
        calendar.setTime(date);
        return parse(calendar);
    }

    public Calendar getCalendar(TimeZone timeZone) {
        return ProtocolTools.createCalendar(date[0] + 2000, date[1], date[2], 0, 0, 0, 0, timeZone);
    }

    public ReferenceDate parse(Calendar calendar) {
        this.date = new byte[3];
        this.date[0] = (byte) (calendar.get(Calendar.YEAR) - 2000);
        this.date[1] = (byte) (calendar.get(Calendar.MONTH) + 1);
        this.date[2] = (byte) calendar.get(Calendar.DATE);
        return this;
    }

    public int getLength() {
        return LENGTH;
    }

    public void setDate(byte[] date) {
        this.date = date.clone();
    }

    public void addOneDay() throws CTRParsingException {
        date[2] = (byte) (date[2] + 1);
        parse(date, 0);
    }

    public static ReferenceDate getReferenceDate(int daysAhead) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, daysAhead);
        return new ReferenceDate().parse(calendar);
    }

}