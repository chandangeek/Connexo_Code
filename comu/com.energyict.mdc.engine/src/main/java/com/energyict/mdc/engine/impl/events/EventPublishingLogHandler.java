/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events;

import com.energyict.mdc.engine.events.ComServerEvent;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.logging.LogLevelMapper;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Provides an implementation for the Handler class
 * that publishes a {@link com.energyict.mdc.engine.events.ComServerEvent}
 * containing the human readable format of the log message.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-07 (16:33)
 */
public abstract class EventPublishingLogHandler extends Handler {

    private final EventPublisher eventPublisher;
    private final AbstractComServerEventImpl.ServiceProvider serviceProvider;

    protected EventPublishingLogHandler(EventPublisher eventPublisher, AbstractComServerEventImpl.ServiceProvider serviceProvider) {
        super();
        this.eventPublisher = eventPublisher;
        this.serviceProvider = serviceProvider;
    }

    @Override
    public void publish (LogRecord record) {
        this.eventPublisher.publish(
                this.toEvent(
                        this.serviceProvider,
                        LogLevelMapper.forJavaUtilLogging().toLogLevel(record.getLevel()),
                        this.extractInfo(record)));
    }

    /**
     * Creates a {@link ComServerEvent} from the human readable log message.
     *
     * @param serviceProvider The ServiceProvider
     * @param logMessage The human readable log message
     * @param level The logging level at which the message was emitted
     * @return The ComServerEvent
     */
    protected abstract ComServerEvent toEvent (AbstractComServerEventImpl.ServiceProvider serviceProvider, LogLevel level, String logMessage);

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