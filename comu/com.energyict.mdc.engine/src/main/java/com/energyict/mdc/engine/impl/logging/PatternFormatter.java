/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Provides an implementation of a java.util.logging.Formatter
 * that simply publishes LogRecords as specified by the pattern
 * and avoids including the source of the LogRecord.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-06-08 (13:31)
 */
public class PatternFormatter extends Formatter {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    public PatternFormatter () {
        super();
    }

    /**
     * Format the given LogRecord.
     *
     * @param record the log record to be formatted.
     * @return a formatted log record
     */
    public synchronized String format (LogRecord record) {
        StringBuilder sb = new StringBuilder();
        String message = this.formatMessage(record);
        sb.append(record.getLevel().getLocalizedName());
        sb.append(": ");
        sb.append(message);
        sb.append(LINE_SEPARATOR);
        if (record.getThrown() != null) {
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
                pw.close();
                sb.append(sw.toString());
            }
            catch (Exception ex) {
                // Avoid that logging component is responsible for canning the app
            }
        }
        return sb.toString();
    }

}