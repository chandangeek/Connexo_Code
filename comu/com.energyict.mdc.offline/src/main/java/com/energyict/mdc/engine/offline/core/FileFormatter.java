/*
 * FileFormatter.java
 *
 * Created on 2 januari 2003, 17:28
 */

package com.energyict.mdc.engine.offline.core;

import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * @author Koen
 */
public class FileFormatter extends Formatter {
    private DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);

    public String format(LogRecord record) {
        String terminator = System.getProperty("line.separator", "\n");
        return format.format(new Date(record.getMillis())) +
                " " +
                record.getLevel() +
                ": " +
                formatMessage(record) +
                " (" +
                record.getSourceClassName() +
                "." +
                record.getSourceMethodName() +
                ")" +
                terminator;
    }

}