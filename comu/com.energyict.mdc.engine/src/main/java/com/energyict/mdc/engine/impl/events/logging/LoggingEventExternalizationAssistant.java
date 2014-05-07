package com.energyict.mdc.engine.impl.events.logging;

import com.energyict.mdc.engine.events.LoggingEvent;
import com.energyict.mdc.engine.impl.logging.LogLevel;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Supports the externalization mechanism for {@link LoggingEvent}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-08 (11:20)
 */
public final class LoggingEventExternalizationAssistant {

    private static final int NULL_INDICATOR = -1;

    public static void writeExternal (LoggingEvent event, ObjectOutput out) throws IOException {
        writeLogLevel(out, event.getLogLevel());
        writeLogMessage(out, event.getLogMessage());
    }

    private static void writeLogLevel (ObjectOutput out, LogLevel logLevel) throws IOException {
        if (logLevel == null) {
            out.writeInt(NULL_INDICATOR);
        }
        else {
            out.writeInt(logLevel.ordinal());
        }
    }

    private static void writeLogMessage (ObjectOutput out, String logMessage) throws IOException {
        if (logMessage == null) {
            out.writeInt(NULL_INDICATOR);
        }
        else {
            out.writeInt(logMessage.length());
            out.writeUTF(logMessage);
        }
    }

    public static LoggingEventPojo readExternal (ObjectInput in) throws IOException {
        LoggingEventPojo pojo = new LoggingEventPojo();
        pojo.readLogLevel(in);
        pojo.readLogMessage(in);
        return pojo;
    }

    public static class LoggingEventPojo {
        private LogLevel logLevel;
        private String logMessage;

        public LogLevel getLogLevel () {
            return logLevel;
        }

        public String getLogMessage () {
            return logMessage;
        }

        private void readLogLevel (ObjectInput in) throws IOException {
            int logLevelOrdinal = in.readInt();
            if (logLevelOrdinal == NULL_INDICATOR) {
                this.logLevel = null;
            }
            else {
                this.logLevel = LogLevel.values()[logLevelOrdinal];
            }
        }

        private void readLogMessage (ObjectInput in) throws IOException {
            int logMessageLength = in.readInt();
            if (logMessageLength == NULL_INDICATOR) {
                this.logMessage = null;
            }
            else {
                this.logMessage = in.readUTF();
            }
        }

    }

    // Hide utility class constructor
    private LoggingEventExternalizationAssistant () {}

}