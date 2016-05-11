package com.energyict.mdc.common;

/**
 * This is a helper class to generate the date time format pattern based on the separate passed in formats
 */

public class DateTimeFormatGenerator {

    public enum Mode { LONG, SHORT };

    public static String getDateTimeFormat(String dateFormat, String timeFormat, String dateTimeOrderFormat, String separatorFormat) {
        StringBuilder dateTimeFormatBuilder = new StringBuilder();

        if (dateTimeOrderFormat.startsWith("T")) {
            dateTimeFormatBuilder.append(timeFormat);
        } else {
            dateTimeFormatBuilder.append(dateFormat);
        }
        if ("SPACE".equals(separatorFormat) ) {
            dateTimeFormatBuilder.append(" ");
        } else {
            dateTimeFormatBuilder.append(" "+separatorFormat.trim()+" ");
        }
        if (dateTimeOrderFormat.startsWith("T")) {
            dateTimeFormatBuilder.append(dateFormat);
        } else {
            dateTimeFormatBuilder.append(timeFormat);
        }
        return dateTimeFormatBuilder.toString();
    }
}
