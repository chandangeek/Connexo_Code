package com.energyict.protocolimplv2.messages.enums;

import java.text.DateFormatSymbols;


public class DaysOfWeek{

    private static final int INT = 7;
    public static final String ALL_DAYS = "All days";
    public static final int DLMS_NOT_SPECIFIED = 0xFF;

    private static String[] daysOfWeek = new String[INT + 1];

    public static String[] getDaysOfWeek(){
        for (int i = 1; i< INT; i++){
            daysOfWeek[i-1] = getDayOfWeekLocalized(i+1);
        }
        // Need to get Monday = 1 and Sunday = 7
        daysOfWeek[INT - 1] = getDayOfWeekLocalized(1);
        daysOfWeek[INT + 0] = ALL_DAYS;
        return daysOfWeek;
    }

    public static String getDayOfWeekLocalized(int day) {
        return new DateFormatSymbols().getWeekdays()[day];
    }

    public static int getDlmsEncoding(String selectedDayOfWeek) {
        if (ALL_DAYS.equals(selectedDayOfWeek)){
            return DLMS_NOT_SPECIFIED; // quite redundant with the final return statement, but for the sake of clarity
        }

        for (int i = 0; i < INT; i++){
            if (daysOfWeek[i].equals(selectedDayOfWeek)){
                return i+1;
            }
        }

        return DLMS_NOT_SPECIFIED;
    }
}