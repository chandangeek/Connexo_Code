/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.logging;

import com.energyict.mdc.engine.config.ComServer;

import java.util.logging.Level;

/**
 * Maps between various log level definitions such as:
 * <ul>
 * <li>{@link ComServer.LogLevel}</li>
 * <li>{@link LogLevel}</li>
 * <li>{@link Level}</li>
 * </ul>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-14 (13:41)
 */
public final class LogLevelMapper {

    public static JavaUtilLoggerMapper forJavaUtilLogging () {
        return new JavaUtilLoggerMapper();
    }

    public static ComServerLogLevelMapper forComServerLogLevel() {
        return new ComServerLogLevelMapper();
    }

    private LogLevelMapper () {super();}

    private static LogLevel comserverLogLevelToLogLevel (ComServer.LogLevel comServerLogLevel) {
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

    private static Level comServerLogLevelToJavaUtilLogging(ComServer.LogLevel logLevel) {
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
                return Level.ALL;
            }
        }
    }

    private static ComServer.LogLevel javaUtilLoggingToComServerLogLevel(Level level) {
        if (Level.INFO.equals(level)) {
            return ComServer.LogLevel.INFO;
        }
        else if (Level.WARNING.equals(level)) {
            return ComServer.LogLevel.WARN;
        }
        else if (Level.SEVERE.equals(level)) {
            return ComServer.LogLevel.ERROR;
        }
        else if (Level.FINE.equals(level)) {
            return ComServer.LogLevel.DEBUG;
        }
        else if (Level.FINER.equals(level)) {
            return ComServer.LogLevel.DEBUG;
        }
        else if (Level.FINEST.equals(level)) {
            return ComServer.LogLevel.TRACE;
        }
        else {
            return ComServer.LogLevel.INFO;
        }
    }

    public static final class ComServerLogLevelMapper {

        public LogLevel toLogLevel(ComServer.LogLevel logLevel) {
            return comserverLogLevelToLogLevel(logLevel);
        }

        public ComServer.LogLevel fromLogLevel(LogLevel logLevel) {
            switch (logLevel) {
                case INFO: {
                    return ComServer.LogLevel.INFO;
                }
                case WARN: {
                    return ComServer.LogLevel.WARN;
                }
                case ERROR: {
                    return ComServer.LogLevel.ERROR;
                }
                case DEBUG: {
                    return ComServer.LogLevel.DEBUG;
                }
                case TRACE: {
                    return ComServer.LogLevel.TRACE;
                }
                default: {
                    return ComServer.LogLevel.INFO;
                }
            }
        }

        public Level toJavaUtilLogLevel(ComServer.LogLevel logLevel) {
            return comServerLogLevelToJavaUtilLogging(logLevel);
        }

        public ComServer.LogLevel fromJavaUtilLogLevel(Level level) {
            return javaUtilLoggingToComServerLogLevel(level);
        }

    }

    public static final class JavaUtilLoggerMapper {

        public ComServer.LogLevel toComServerLogLevel (Level level) {
            return javaUtilLoggingToComServerLogLevel(level);
        }

        public Level fromComServerLogLevel (ComServer.LogLevel logLevel) {
            return comServerLogLevelToJavaUtilLogging(logLevel);
        }

        public LogLevel toLogLevel (Level level) {
            if (Level.INFO.equals(level)) {
                return LogLevel.INFO;
            }
            else if (Level.WARNING.equals(level)) {
                return LogLevel.WARN;
            }
            else if (Level.SEVERE.equals(level)) {
                return LogLevel.ERROR;
            }
            else if (Level.FINE.equals(level)) {
                return LogLevel.DEBUG;
            }
            else if (Level.FINER.equals(level)) {
                return LogLevel.DEBUG;
            }
            else if (Level.FINEST.equals(level)) {
                return LogLevel.TRACE;
            }
            else {
                return LogLevel.INFO;
            }
        }

        public Level fromLogLevel (LogLevel logLevel) {
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

        private JavaUtilLoggerMapper() {}

    }

}