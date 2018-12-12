package com.energyict.protocolimpl.iec1107.emh.nxt4;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


/**
 * Contains the time and date values for the EMH NXT4 protocol
 *
 * @author sva
 * @since 5/11/2014 - 16:12
 */
public class DateTime {

    private final TimeZone timeZone;
    private final String dateFormat;

    /**
     * Private constructor
     */
    public DateTime(TimeZone timeZone, String dateFormat) {
        this.timeZone = timeZone;
        this.dateFormat = dateFormat;
    }

    public Date parseDate(String dateTimeString) throws IOException, ParseException {
        if (dateTimeString.length() != dateFormat.length()) {
            throw new IOException("The dateTime did not contain enough characters to match the given date format (" + dateFormat + ")");
        }
        StringBuilder dateTimeBuilder = new StringBuilder(dateTimeString);
        StringBuilder dateFormatBuilder = new StringBuilder(dateFormat);

        TimeZone timeZone = this.timeZone;
        int index = dateFormatBuilder.indexOf("z"); // 'season identification': 0 = normal time, 1 = summer time, 2 = UTC
        if (index != -1) {
            int timeZoneIndication = dateTimeString.charAt(index);
            if (timeZoneIndication == '2') {
                timeZone = TimeZone.getTimeZone("UTC");
            }
            dateFormatBuilder.deleteCharAt(index);
            dateTimeBuilder.deleteCharAt(index);
        }

        index = dateFormatBuilder.indexOf("n");  // 'day of week': 1..7, 1 = Monday - this should be filtered out of the date format
        if (index != -1) {
            dateFormatBuilder.deleteCharAt(index);
            dateTimeBuilder.deleteCharAt(index);
        }

        DateFormat dateFormatter = new SimpleDateFormat(dateFormatBuilder.toString());
        dateFormatter.setTimeZone(timeZone);
        return dateFormatter.parse(dateTimeBuilder.toString());
    }

    public String formatDateTime(Date date) {
        StringBuilder dateTimeBuilder = new StringBuilder();
        StringBuilder dateFormatBuilder = new StringBuilder(dateFormat);

        int seasonIdentificationIndex = dateFormatBuilder.indexOf("z"); // 'season identification': 0 = normal time, 1 = summer time, 2 = UTC
        if (seasonIdentificationIndex != -1) {
            dateFormatBuilder.deleteCharAt(seasonIdentificationIndex);
        }

        int dayOfWeekIdentificationIndex = dateFormatBuilder.indexOf("n");  // 'day of week': 1..7, 1 = Monday - this should be filtered out of the date format
        if (dayOfWeekIdentificationIndex != -1) {
            dateFormatBuilder.deleteCharAt(dayOfWeekIdentificationIndex);
        }

        DateFormat dateFormatter = new SimpleDateFormat(dateFormatBuilder.toString());
        dateFormatter.setTimeZone(timeZone);
        dateTimeBuilder.append(dateFormatter.format(date));

        if (dayOfWeekIdentificationIndex != -1) {
            dateTimeBuilder.insert(dayOfWeekIdentificationIndex, '0');  // Leave 'day of week' unspecified
        }
        if (seasonIdentificationIndex != -1) {
            dateTimeBuilder.insert(seasonIdentificationIndex, timeZone.inDaylightTime(date) ? '1' : '0');
        }

        return dateTimeBuilder.toString();
    }
}