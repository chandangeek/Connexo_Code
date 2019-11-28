/*
 * ScreenFormatter.java
 *
 * Created on 2 januari 2003, 16:27
 */

package com.energyict.mdc.engine.offline.core;

import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * @author Koen
 */
public class ScreenFormatter extends Formatter {
    int iID;
    String strLoggerName;
    private DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

    public ScreenFormatter(String strLoggerName) {
        super();
        this.strLoggerName = strLoggerName;
    }

    public String format(LogRecord record) {
        final String strRecord = formatMessage(record); //KV 22112002

        String substr = record.getLoggerName().substring(strLoggerName.length());


        if (substr.compareTo("") == 0) {
            iID = -1;
        } else {
            iID = TaskLogReference.fromReferenceString(substr).getProtocolReaderId();
        }

        return
                format.format(new Date(record.getMillis())) +
                        " " +
                        strRecord;
    }

    public int getID() {
        return iID;
    }

} // private class ScreenFormatter extends Formatter
