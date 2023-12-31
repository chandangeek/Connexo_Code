/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.inbound;

import com.energyict.mdc.engine.impl.logging.LogLevelMapper;

import java.text.MessageFormat;
import java.time.Clock;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Provides an implementation for the Handler class
 * that creates ComSessionJournalEntries or ComTaskMessageJournalEntries
 * depending on the state of the {@link InboundDiscoveryContextImpl} it is working in.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-24 (13:39)
 */
class DiscoveryContextLogHandler extends Handler {

    private final Clock clock;
    private InboundDiscoveryContextImpl context;

    DiscoveryContextLogHandler(Clock clock, InboundDiscoveryContextImpl context) {
        super();
        this.clock = clock;
        this.context = context;
    }

    @Override
    public void publish(LogRecord record) {
        this.context.addJournalEntry(
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