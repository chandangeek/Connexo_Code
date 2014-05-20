package com.energyict.mdc.engine.impl.core.aspects.logging;

import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.inbound.ComPortRelatedComChannel;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Provides an implementation for the Handler class
 * that creates {@link com.energyict.mdc.journal.ComSessionJournalEntry ComSessionJournalEntries}
 * or {@link com.energyict.mdc.journal.ComTaskExecutionMessageJournalEntry ComTaskExecutionMessageJournalEntries}
 * each time bytes have been read or written to a {@link ComPortRelatedComChannel}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-03-14 (10:17)
 */
public class ComChannelLogHandler extends Handler {

    private ExecutionContext executionContext;

    public ComChannelLogHandler (ExecutionContext executionContext) {
        super();
        this.executionContext = executionContext;
    }

    @Override
    public void publish (LogRecord record) {
        this.executionContext.createJournalEntry(this.extractInfo(record));
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