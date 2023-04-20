/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.connexo.user;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class ComChannelLogHandler extends FileHandler {

    public ComChannelLogHandler() throws IOException, SecurityException {
        super();
        this.setFormatter(new ComChannelLogFormatter());
    }

    private static class ComChannelLogFormatter extends Formatter {

        @Override
        public synchronized String format(LogRecord record) {
            return extractInfo(record);
        }

        private String extractInfo(LogRecord record) {
            String messageFormat = this.getMessageFormat(record);
            Object[] args = record.getParameters();
            return LocalDateTime.now().toString() + " " + args[0] + " " + args[1] + " " + messageFormat + "\n";
        }

        private String getMessageFormat(LogRecord record) {
            ResourceBundle rb = record.getResourceBundle();
            String messageInRecord = record.getMessage();
            if (rb != null) {
                try {
                    return rb.getString(messageInRecord);
                } catch (MissingResourceException ex) {
                    // key not found, so messageInRecord is key
                    return messageInRecord;
                }
            } else {
                return messageInRecord;
            }
        }
    }

}
