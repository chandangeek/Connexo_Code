package com.energyict.protocolimpl.dlms.common;

import com.energyict.dlms.DataStructure;

import java.util.Calendar;

public final class LoadProfileUtil {

    public static Calendar setCalendar(Calendar cal, DataStructure dataStructure, byte btype, int profileInterval) {

        Calendar calendar = (Calendar) cal.clone();

        if (dataStructure.getOctetString(0).getArray()[0] != -1) {
            calendar.set(Calendar.YEAR, (((int) dataStructure.getOctetString(0).getArray()[0] & 0xff) << 8) |
                    (((int) dataStructure.getOctetString(0).getArray()[1] & 0xff)));
        }


        if (dataStructure.getOctetString(0).getArray()[2] != -1) {
            calendar.set(Calendar.MONTH, ((int) dataStructure.getOctetString(0).getArray()[2] & 0xff) - 1);
        }


        if (dataStructure.getOctetString(0).getArray()[3] != -1) {
            calendar.set(Calendar.DAY_OF_MONTH, ((int) dataStructure.getOctetString(0).getArray()[3] & 0xff));
        }


        if (dataStructure.getOctetString(0).getArray()[5] != -1) {
            calendar.set(Calendar.HOUR_OF_DAY, ((int) dataStructure.getOctetString(0).getArray()[5] & 0xff));
        } else {
            calendar.set(Calendar.HOUR_OF_DAY, 0);
        }


        if (btype == 0) {
            if (dataStructure.getOctetString(0).getArray()[6] != -1) {
                calendar.set(Calendar.MINUTE, (((int) dataStructure.getOctetString(0).getArray()[6] & 0xff) / (profileInterval / 60)) * (profileInterval / 60));
            } else {
                calendar.set(Calendar.MINUTE, 0);
            }

            calendar.set(Calendar.SECOND, 0);
        } else {
            if (dataStructure.getOctetString(0).getArray()[6] != -1) {
                calendar.set(Calendar.MINUTE, ((int) dataStructure.getOctetString(0).getArray()[6] & 0xff));
            } else {
                calendar.set(Calendar.MINUTE, 0);
            }

            if (dataStructure.getOctetString(0).getArray()[7] != -1) {
                calendar.set(Calendar.SECOND, ((int) dataStructure.getOctetString(0).getArray()[7] & 0xff));
            } else {
                calendar.set(Calendar.SECOND, 0);
            }
        }

        // if DSA, add 1 hour
        if (dataStructure.getOctetString(0).getArray()[11] != -1) {
            if ((dataStructure.getOctetString(0).getArray()[11] & (byte) 0x80) == 0x80) {
                calendar.add(Calendar.HOUR_OF_DAY, -1);
            }
        }

        return calendar;

    }

}
