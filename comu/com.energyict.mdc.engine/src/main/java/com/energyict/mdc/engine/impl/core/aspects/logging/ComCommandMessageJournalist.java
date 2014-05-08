package com.energyict.mdc.engine.impl.core.aspects.logging;

import com.elster.jupiter.util.time.Clock;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Provides an implementation for the Handler class
 * that publishes a {@link com.energyict.mdc.journal.ComTaskExecutionMessageJournalEntry}
 * containing the human readable format of the log message.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-02-04 (16:36)
 */
public class ComCommandMessageJournalist extends Handler {

    private final Clock clock;
    private final ComTaskExecutionSessionShadow comTaskExecutionSessionShadow;

    public ComCommandMessageJournalist(Clock clock, ComTaskExecutionSessionShadow comTaskExecutionSessionShadow) {
        super();
        this.clock = clock;
        this.comTaskExecutionSessionShadow = comTaskExecutionSessionShadow;
    }

    @Override
    public void publish (LogRecord record) {
        ComTaskExecutionMessageJournalEntryShadow journalEntryShadow = new ComTaskExecutionMessageJournalEntryShadow();
        journalEntryShadow.setTimestamp(this.clock.now());
        journalEntryShadow.setMessage(this.extractInfo(record));
        this.comTaskExecutionSessionShadow.addComTaskJournalEntry(journalEntryShadow);
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