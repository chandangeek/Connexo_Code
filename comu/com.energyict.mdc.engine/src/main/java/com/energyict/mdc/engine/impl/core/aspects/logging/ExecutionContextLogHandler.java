package com.energyict.mdc.engine.impl.core.aspects.logging;

import com.energyict.mdc.engine.impl.core.ScheduledJobImpl;

import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.StackTracePrinter;
import com.energyict.mdc.tasks.history.ComSessionBuilder;
import com.energyict.mdc.tasks.history.ComTaskExecutionSessionBuilder;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Provides an implementation for the Handler class
 * that creates ComSessionJournalEntries
 * or ComTaskMessageJournalEntries
 * depending on the state of the {@link ScheduledJobImpl.ExecutionContext} it is working in.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-08 (15:44)
 */
public class ExecutionContextLogHandler extends Handler {

    private final Clock clock;
    private final ScheduledJobImpl.ExecutionContext executionContext;

    public ExecutionContextLogHandler(Clock clock, ScheduledJobImpl.ExecutionContext executionContext) {
        super();
        this.clock = clock;
        this.executionContext = executionContext;
    }

    @Override
    public void publish (LogRecord record) {
        ComTaskExecutionSessionBuilder taskExecutionSession = this.executionContext.getCurrentTaskExecutionBuilder();
        if (taskExecutionSession != null) {
            this.publishComTaskMessageJournalEntry(taskExecutionSession, record);
        }
        else {
            this.publishComSessionJournalEntry(this.executionContext.getComSessionBuilder(), record);
        }
    }

    private void publishComTaskMessageJournalEntry (ComTaskExecutionSessionBuilder taskExecutionSession, LogRecord record) {
        Throwable thrown = record.getThrown();
        taskExecutionSession.addComTaskExecutionMessageJournalEntry(clock.now(), thrown == null ? "" : StackTracePrinter.print(thrown), extractInfo(record));
    }

    private void publishComSessionJournalEntry (ComSessionBuilder builder, LogRecord record) {
        builder.addJournalEntry(clock.now(), extractInfo(record), record.getThrown());
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