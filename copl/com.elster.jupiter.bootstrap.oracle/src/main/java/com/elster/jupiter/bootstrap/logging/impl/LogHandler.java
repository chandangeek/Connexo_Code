package com.elster.jupiter.bootstrap.logging.impl;

import com.elster.jupiter.util.OsgiLogLevelMapper;
import org.osgi.service.log.LogService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

class LogHandler extends Handler {

	private final LogService logService;
    private final String format;

    LogHandler(LogService logService, String format) {
        this.format = format;
		this.logService = logService;
	}
	
	@Override
	public void close() {
	}

	@Override
	public void flush() {
	}

	@Override
	public void publish(LogRecord logRecord) {
        int osgiLevel = OsgiLogLevelMapper.mapLevel(logRecord.getLevel());
		logService.log(osgiLevel, format(logRecord) , logRecord.getThrown());
	}

    private String format(LogRecord record) {
        return String.format(format,
                dateFrom(record),
                sourceFrom(record),
                record.getLoggerName(),
                record.getLevel().getLocalizedName(),
                formatMessage(record),
                throwableFrom(record));
    }

    private String throwableFrom(LogRecord record) {
        if (record.getThrown() == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println();
        record.getThrown().printStackTrace(pw);
        pw.close();
        return sw.toString();
    }

    private String sourceFrom(LogRecord record) {
        String source;
        if (record.getSourceClassName() != null) {
            source = record.getSourceClassName();
            if (record.getSourceMethodName() != null) {
                source += " " + record.getSourceMethodName();
            }
        } else {
            source = record.getLoggerName();
        }
        return source;
    }

    private Date dateFrom(LogRecord record) {
        return new Date(record.getMillis());
    }

    private String formatMessage(LogRecord record) {
        String format = record.getMessage();
        java.util.ResourceBundle catalog = record.getResourceBundle();
        if (catalog != null) {
            try {
                format = catalog.getString(record.getMessage());
            } catch (java.util.MissingResourceException ex) {
                // Drop through.  Use record message as format
                format = record.getMessage();
            }
        }
        // Do the formatting.
        try {
            Object parameters[] = record.getParameters();
            if (parameters == null || parameters.length == 0) {
                // No parameters.  Just return format string.
                return format;
            }
            // Is it a java.text style format?
            // Ideally we could match with
            // Pattern.compile("\\{\\d").matcher(format).find())
            // However the cost is 14% higher, so we cheaply check for
            // 1 of the first 4 parameters
            if (format.indexOf("{0") >= 0 || format.indexOf("{1") >=0 ||
                    format.indexOf("{2") >=0|| format.indexOf("{3") >=0) {
                return java.text.MessageFormat.format(format, parameters);
            }
            return format;

        } catch (Exception ex) {
            // Formatting failed: use localized format string.
            return format;
        }
    }

}
