/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.logging;

import com.energyict.mdc.common.StackTracePrinter;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSessionBuilder;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevelMapper;

import java.text.MessageFormat;
import java.time.Clock;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Provides an implementation for the Handler class
 * that creates ComSessionJournalEntries
 * or ComTaskMessageJournalEntries
 * depending on the state of the {@link com.energyict.mdc.engine.impl.core.ExecutionContext} it is working in.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-08 (15:44)
 */
public class ExecutionContextLogHandler extends Handler {

    private final Clock clock;
    private final ExecutionContext executionContext;

    public ExecutionContextLogHandler(Clock clock, ExecutionContext executionContext) {
        super();
        this.clock = clock;
        this.executionContext = executionContext;
    }

    @Override
    public void publish (LogRecord record) {
        Optional<ComTaskExecutionSessionBuilder> taskExecutionSession = this.executionContext.getCurrentTaskExecutionBuilder();
        if (taskExecutionSession.isPresent()) {
            this.publishComTaskMessageJournalEntry(taskExecutionSession.get(), record);
        }
        else {
            this.publishComSessionJournalEntry(this.executionContext.getComSessionBuilder(), record);
        }
    }

    private void publishComTaskMessageJournalEntry (ComTaskExecutionSessionBuilder taskExecutionSession, LogRecord record) {
        String errorDesciption;
        if (record.getThrown() == null) {
            errorDesciption = "";
        }
        else {
            errorDesciption = StackTracePrinter.print(record.getThrown());
        }
        taskExecutionSession.addComTaskExecutionMessageJournalEntry(
                this.clock.instant(),
                LogLevelMapper.forComServerLogLevel().fromJavaUtilLogLevel(record.getLevel()),
                extractInfo(record),
                errorDesciption);
    }

    private void publishComSessionJournalEntry (ComSessionBuilder builder, LogRecord record) {
        builder.addJournalEntry(
                this.clock.instant(),
                LogLevelMapper.forComServerLogLevel().fromJavaUtilLogLevel(record.getLevel()),
                extractInfo(record),
                record.getThrown());
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