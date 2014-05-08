package com.energyict.mdc.engine.impl.scheduling.aspects.logging;

import com.energyict.mdc.engine.impl.core.ScheduledJobImpl;
import com.energyict.comserver.time.Clocks;
import com.energyict.mdc.journal.StackTracePrinter;
import com.energyict.mdc.shadow.journal.ComSessionJournalEntryShadow;
import com.energyict.mdc.shadow.journal.ComSessionShadow;
import com.energyict.mdc.shadow.journal.ComTaskExecutionMessageJournalEntryShadow;
import com.energyict.mdc.shadow.journal.ComTaskExecutionSessionShadow;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Provides an implementation for the Handler class
 * that creates {@link com.energyict.mdc.journal.ComSessionJournalEntry ComSessionJournalEntries}
 * or {@link com.energyict.mdc.journal.ComTaskExecutionMessageJournalEntry ComTaskMessageJournalEntries}
 * depending on the state of the {@link ScheduledJobImpl.ExecutionContext} it is working in.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-08 (15:44)
 */
public class ExecutionContextLogHandler extends Handler {

    private ScheduledJobImpl.ExecutionContext executionContext;

    public ExecutionContextLogHandler (ScheduledJobImpl.ExecutionContext executionContext) {
        super();
        this.executionContext = executionContext;
    }

    @Override
    public void publish (LogRecord record) {
        ComTaskExecutionSessionShadow taskExecutionSession = this.executionContext.getCurrentTaskExecutionSession();
        if (taskExecutionSession != null) {
            this.publishComTaskMessageJournalEntry(taskExecutionSession, record);
        }
        else {
            this.publishComSessionJournalEntry(this.executionContext.getComSessionShadow(), record);
        }
    }

    private void publishComTaskMessageJournalEntry (ComTaskExecutionSessionShadow taskExecutionSession, LogRecord record) {
        ComTaskExecutionMessageJournalEntryShadow shadow = new ComTaskExecutionMessageJournalEntryShadow();
        shadow.setMessage(this.extractInfo(record));
        shadow.setTimestamp(Clocks.getAppServerClock().now());
        Throwable thrown = record.getThrown();
        if (thrown != null) {
            shadow.setErrorDescription(StackTracePrinter.print(thrown));
        }
        taskExecutionSession.addComTaskJournalEntry(shadow);
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