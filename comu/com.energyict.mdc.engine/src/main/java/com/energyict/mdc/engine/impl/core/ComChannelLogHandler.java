/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.tasks.history.ComSessionJournalEntry;
import com.energyict.mdc.common.tasks.history.ComTaskExecutionMessageJournalEntry;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Provides an implementation for the Handler class
 * that creates {@link ComSessionJournalEntry ComSessionJournalEntries}
 * or {@link ComTaskExecutionMessageJournalEntry ComTaskExecutionMessageJournalEntries}
 * each time bytes have been read or written to a {@link ComPortRelatedComChannel}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-03-14 (10:17)
 */
class ComChannelLogHandler extends FileHandler {

    public ComChannelLogHandler() throws IOException, SecurityException {
        super();
        this.setFormatter(new ComChannelLogFormatter());
    }

    private static class ComChannelLogFormatter extends Formatter {

        @Override
        public String format(LogRecord record) {
            return extractInfo(record);
        }

        private String extractInfo(LogRecord record) {
            String messageFormat = this.getMessageFormat(record);
            Object[] args = record.getParameters();
            if (args == null || args.length == 0) {
                return messageFormat;
            } else {
                return MessageFormat.format(messageFormat, args);
            }
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
