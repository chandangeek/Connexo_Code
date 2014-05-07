package com.energyict.mdc.engine.impl.events;

import com.energyict.mdc.engine.events.ComServerEvent;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.logging.LogLevelMapper;
import com.energyict.comserver.time.Clocks;

import java.text.MessageFormat;
import java.util.Date;
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

    @Override
    public void publish (LogRecord record) {
        EventPublisherImpl.getInstance().
            publish(
                this.toEvent(
                        Clocks.getAppServerClock().now(),
                        LogLevelMapper.toComServerLogLevel(record.getLevel()),
                        this.extractInfo(record)));
    }

    /**
     * Creates a {@link ComServerEvent} from the human readable log message.
     *
     * @param eventOccurrenceTimestamp The Timestamp on which the event occurred
     * @param logMessage The human readable log message
     * @param level The logging level at which the message was emitted
     * @return The ComServerEvent
     */
    protected abstract ComServerEvent toEvent (Date eventOccurrenceTimestamp, LogLevel level, String logMessage);

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