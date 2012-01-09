package com.elster.protocolimpl.dlms.util;

import com.elster.dlms.types.basic.*;
import com.energyict.cbo.BusinessException;

/**
 * User: heuckeg
 * Date: 04.10.11
 * Time: 14:35
 */
@SuppressWarnings({"unused"})
public class RepetitiveDate {

    private final static String[] weekdays = {"MO", "TU", "WE", "TH", "FR", "SA", "SU"};

    public static void checkRepetitiveDate(String dateString, String name) throws BusinessException {

        try {
            dateStringToDlmsDateTime(dateString);
        } catch (Exception e) {
            throw new BusinessException(name + ": " + e.getMessage());
        }
    }


    public static DlmsDateTime dateStringToDlmsDateTime(String dateString) throws BusinessException {

        if ((dateString == null) || (dateString.length() == 0)) {
            throw new BusinessException(" is 'null' or empty.");
        }

        String[] part = dateString.split(" ");
        if (part.length != 2) {
            throw new BusinessException(" missing date or time part.");
        }
        DlmsDate date = dateStringToDlmsDate(part[0]);
        DlmsTime time = dateStringToDlmsTime(part[1]);
        return new DlmsDateTime(date, time);

    }

    public static DlmsDate dateStringToDlmsDate(String dateString) throws BusinessException {

        DlmsDate result;
        try {
            String d = dateString.trim();
            if (isWeekdayDefinition(dateString)) {
                return new DlmsDate(DlmsDate.YEAR_NOT_SPECIFIED, DlmsDate.MONTH_NOT_SPECIFIED, DlmsDate.DAY_OF_MONTH_NOT_SPECIFIED, getDayOfWeek(dateString));
            }

            String[] dateParts = d.split("-");
            if (dateParts.length != 3) {
                throw new BusinessException("date format error");
            }

            int year = DlmsDate.YEAR_NOT_SPECIFIED;
            if (!dateParts[0].trim().equals("*")) {
                year = Integer.parseInt(dateParts[0].trim());
            }

            int month = DlmsDate.MONTH_NOT_SPECIFIED;
            if (!dateParts[1].trim().equals("*")) {
                month = Integer.parseInt(dateParts[1].trim());
            }

            int day = DlmsDate.DAY_OF_MONTH_NOT_SPECIFIED;
            if (!dateParts[2].trim().equals("*")) {
                day = Integer.parseInt(dateParts[2].trim());
            }

            result = new DlmsDate(year, month, day);

        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
        return result;
    }


    private static boolean isWeekdayDefinition(String dateString) {
        return getDayOfWeek(dateString) > 0;
    }

    private static int getDayOfWeek(String weekDay) {
        int i = 0;
        for (String s : weekdays) {
            i++;
            if (s.equalsIgnoreCase(weekDay)) {
                return i;
            }
        }
        return 0;
    }

    public static DlmsTime dateStringToDlmsTime(String timeString) throws BusinessException {

        DlmsTime result;
        try {
            String[] timeParts = timeString.trim().split(":");
            if (timeParts.length < 2) {
                throw new BusinessException("time format error");
            }

            int hour = DlmsTime.NOT_SPECIFIED;
            if (!timeParts[0].trim().equals("*")) {
                hour = Integer.parseInt(timeParts[0].trim());
            }

            int minute = Integer.parseInt(timeParts[1].trim());
            int second = 0;
            if (timeParts.length > 2) {
                second = Integer.parseInt(timeParts[2].trim());
            }

            result = new DlmsTime(hour, minute, second, DlmsTime.NOT_SPECIFIED);

        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
        return result;
    }

}
