package com.energyict.mdc.engine.impl.core.inbound.aspects.logging;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Provides an implementation for the Handler class
 * that creates {@link com.energyict.mdc.journal.ComSessionJournalEntry ComSessionJournalEntries}
 * or {@link com.energyict.mdc.journal.ComTaskExecutionMessageJournalEntry ComTaskMessageJournalEntries}
 * depending on the state of the {@link InboundDiscoveryContextImpl} it is working in.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-24 (13:39)
 */
public class DiscoveryContextLogHandler extends Handler {

    private InboundDiscoveryContextImpl context;

    public DiscoveryContextLogHandler (InboundDiscoveryContextImpl context) {
        super();
        this.context = context;
    }

    @Override
    public void publish(LogRecord record) {
        this.publishComSessionJournalEntry(this.context.getComSessionShadow(), record);
    }

    private void publishComSessionJournalEntry (ComSessionShadow sessionShadow, LogRecord record) {
        ComSessionJournalEntryShadow shadow = new ComSessionJournalEntryShadow();
        shadow.setMessage(this.extractInfo(record));
        shadow.setTimestamp(Clocks.getAppServerClock().now());
        Throwable thrown = record.getThrown();
        if (thrown != null) {
            shadow.setCause(thrown);
        }
        sessionShadow.addJournaleEntry(shadow);
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
        /* No resources to flush.
         * All entries go into the ComSessionShadow
         * and will be 'flushed' when the ComSession
         * is created from the ComSessionShadow. */
    }

    @Override
    public void close () throws SecurityException {
        // No resources to close
    }

}