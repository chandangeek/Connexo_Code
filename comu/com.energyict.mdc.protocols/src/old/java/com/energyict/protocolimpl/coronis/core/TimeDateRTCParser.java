/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.core;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

public class TimeDateRTCParser {

    static public Calendar parse(byte[] data, int offset, int length, TimeZone timeZone) throws IOException {
        return parse(WaveflowProtocolUtils.getSubArray(data, offset, length), timeZone);
    }

    static public Calendar parse(byte[] data, TimeZone timeZone) throws IOException {

        Calendar calendar = Calendar.getInstance(timeZone);


        int offset = 0;
        calendar.set(Calendar.DAY_OF_MONTH, ProtocolUtils.getInt(data, offset++, 1));
        calendar.set(Calendar.MONTH, ProtocolUtils.getInt(data, offset++, 1) - 1);
        calendar.set(Calendar.YEAR, ProtocolUtils.getInt(data, offset++, 1) + 2000);
        offset++; // skip day of week
        calendar.set(Calendar.HOUR_OF_DAY, ProtocolUtils.getInt(data, offset++, 1));
        calendar.set(Calendar.MINUTE, ProtocolUtils.getInt(data, offset++, 1));
        if (data.length == 7) {
            calendar.set(Calendar.SECOND, ProtocolUtils.getInt(data, offset++, 1));
        } else {
            calendar.set(Calendar.SECOND, 0);
        }

        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    static public byte[] utcTimeFrame6() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT")); //protocolLink.getTimeZone());
        return prepare6(calendar);
    }


    static public byte[] prepare(Calendar calendar) {
        byte[] data = new byte[7];

        data[0] = (byte) calendar.get(Calendar.DAY_OF_MONTH);
        data[1] = (byte) (calendar.get(Calendar.MONTH) + 1);
        data[2] = (byte) (calendar.get(Calendar.YEAR) - 2000);
        data[3] = (byte) (calendar.get(Calendar.DAY_OF_WEEK) - 1);
        data[4] = (byte) calendar.get(Calendar.HOUR_OF_DAY);
        data[5] = (byte) calendar.get(Calendar.MINUTE);
        data[6] = (byte) calendar.get(Calendar.SECOND);

        return data;
    }

    static public byte[] prepare6(Calendar calendar) {
        byte[] data = new byte[6];

        data[0] = (byte) calendar.get(Calendar.DAY_OF_MONTH);
        data[1] = (byte) (calendar.get(Calendar.MONTH) + 1);
        data[2] = (byte) (calendar.get(Calendar.YEAR) - 2000);
        data[3] = (byte) (calendar.get(Calendar.DAY_OF_WEEK) - 1);
        data[4] = (byte) calendar.get(Calendar.HOUR_OF_DAY);
        data[5] = (byte) calendar.get(Calendar.MINUTE);

        return data;
    }


    static public int size() {
        return 7;
    }

}
