package com.energyict.mdc.engine.impl.core.logging;

import com.energyict.mdc.engine.impl.core.JournalEntryFactory;
import com.energyict.mdc.engine.impl.logging.LogLevelMapper;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Provides an implementation for the Handler class
 * that publishes a ComTaskExecutionMessageJournalEntry
 * containing the human readable format of the log message.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-02-04 (16:36)
 */
public class ComCommandMessageJournalist extends Handler {

    private final JournalEntryFactory journalEntryFactory;

    public ComCommandMessageJournalist(JournalEntryFactory journalEntryFactory) {
        super();
        this.journalEntryFactory = journalEntryFactory;
    }

    @Override
    public void publish (LogRecord record) {
        this.journalEntryFactory.createJournalEntry(
                LogLevelMapper.forJavaUtilLogging().toComServerLogLevel(record.getLevel()),
                extractInfo(record));
    }

    private String extractInfo (LogRecord record) {
        String messageFormat = this.getMessageFormat(record);
        Object[] args = record.getParameters();
        if (args == null || args.length == 0) {
            return messageFormat;
        }
        else {
            return MessageFormat.format(messageFormat, args);
        }
    }

    private String getMessageFormat (LogRecord record) {
        ResourceBundle rb = record.getResourceBundle();
        String messageInRecord = record.getMessage();
        if (rb != null) {
            try {
                return rb.getString(messageInRecord);
            }
            catch (MissingResourceException ex) {
                // key not found, so messageInRecord is key
                return messageInRecord;
            }
        }
        else {
            return messageInRecord;
        }
    }

    @Override
    public void flush () {
        // Nothing to flush
    }

    @Override
    public void close () throws SecurityException {
        // Nothing to close
    }

}