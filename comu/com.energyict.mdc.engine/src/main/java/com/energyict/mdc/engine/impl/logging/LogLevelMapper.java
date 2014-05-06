package com.energyict.mdc.engine.impl.logging;

import com.energyict.mdc.engine.model.ComServer;

import java.util.logging.Level;

/**
 * Maps the ComServer).LogLevel to a {@link LogLevel}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-14 (13:41)
 */
public final class LogLevelMapper {

    /**
     * Maps a ComServer.LogLevel to a {@link LogLevel}.
     *
     * @param comServerLogLevel The ComServer log level
     * @return The LogLevel
     */
    public static LogLevel map (ComServer.LogLevel comServerLogLevel) {
        switch (comServerLogLevel) {
            case INFO: {
                return LogLevel.INFO;
            }
            case WARN: {
                return LogLevel.WARN;
            }
            case ERROR: {
                return LogLevel.ERROR;
            }
            case DEBUG: {
                return LogLevel.DEBUG;
            }
            case TRACE: {
                return LogLevel.TRACE;
            }
            default: {
                return LogLevel.INFO;
            }
        }
    }

    public static Level toJavaUtilLogLevel (LogLevel logLevel) {
        switch (logLevel) {
            case INFO: {
                return Level.INFO;
            }
            case WARN: {
                return Level.WARNING;
            }
            case ERROR: {
                return Level.SEVERE;
            }
            case DEBUG: {
                return Level.FINE;
            }
            case TRACE: {
                return Level.FINEST;
            }
            default: {
                return Level.INFO;
            }
        }
    }

    public static LogLevel toComServerLogLevel (Level logLevel) {
        if (Level.INFO.equals(logLevel)) {
            return LogLevel.INFO;
        }
        else if (Level.WARNING.equals(logLevel)) {
            return LogLevel.WARN;
        }
        else if (Level.SEVERE.equals(logLevel)) {
            return LogLevel.ERROR;
        }
        else if (Level.FINE.equals(logLevel)) {
            return LogLevel.DEBUG;
        }
        else if (Level.FINER.equals(logLevel)) {
            return LogLevel.DEBUG;
        }
        else if (Level.FINEST.equals(logLevel)) {
            return LogLevel.TRACE;
        }
        else {
            return LogLevel.INFO;
        }
    }

    private LogLevelMapper () {super();}

}