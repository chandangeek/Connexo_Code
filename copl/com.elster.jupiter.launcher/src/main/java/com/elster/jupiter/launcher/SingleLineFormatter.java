/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.launcher;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class SingleLineFormatter extends Formatter {

    private Date currentDate = new Date();
    private static final String format = "{0,date} {0,time}";
    private MessageFormat formatter;
    private Object args[] = new Object[1];

    /**
     * Format the given LogRecord.
     * @param record the log record to be formatted.
     * @return a formatted log record
     */
    public synchronized String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();
        currentDate.setTime(record.getMillis());
        args[0] = currentDate;
        StringBuffer text = new StringBuffer();
        if (formatter == null) {
            formatter = new MessageFormat(format);
        }
        formatter.format(args, text, null);
        sb.append(text);
        sb.append(" ");
        String message = formatMessage(record);
        sb.append(record.getLevel().getLocalizedName());
        Arrays.asList(record.getParameters()).stream().findFirst().ifPresent(param -> {
            sb.append(" ").append(param.toString());
            sb.append(" :");
            for(int i=0; i < 8 - record.getLevel().getLocalizedName().length() + 15 - param.toString().length(); i++){
                sb.append(" ");
            }
        });
        sb.append(message);
        String lineSeparator = "\r\n";
        sb.append(lineSeparator);
        if (record.getThrown() != null) {
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
                pw.close();
                sb.append(sw.toString());
            } catch (Exception ex) {
                // Avoid that printing an exception stacktrace produces another stacktrace
            }
        }
        return sb.toString();
    }

}