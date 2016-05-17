package com.elster.jupiter.launcher;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class SingleLineFormatter extends Formatter {

    Date currentDate = new Date();
    private final static String format = "{0,date} {0,time}";
    private MessageFormat formatter;
    private Object args[] = new Object[1];
    private String lineSeparator = "\r\n";

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
        sb.append(": ");
        int iOffset = (1000 - record.getLevel().intValue()) / 100;
        for( int i = 0; i < iOffset;  i++ ){
            sb.append(" ");
        }
        sb.append(message);
        sb.append(lineSeparator);
        if (record.getThrown() != null) {
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
                pw.close();
                sb.append(sw.toString());
            } catch (Exception ex) {
            }
        }
        return sb.toString();
    }
}
