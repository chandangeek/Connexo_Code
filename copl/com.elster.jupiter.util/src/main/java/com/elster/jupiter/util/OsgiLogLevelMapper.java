package com.elster.jupiter.util;

import org.osgi.service.log.LogService;

import java.util.logging.Level;

/**
 * Maps a java.util.logger log level to one of the four osgi log levels.
 */
public enum OsgiLogLevelMapper {
    ;

    public static int mapLevel(Level level) {
        if (level.intValue() <= Level.FINE.intValue()) {
            return LogService.LOG_DEBUG;
        } else if (level.intValue() <= Level.INFO.intValue()) {
            return LogService.LOG_INFO;
        } else if (level.intValue() <= Level.WARNING.intValue()) {
            return LogService.LOG_WARNING;
        } else {
            return LogService.LOG_ERROR;
        }
    }

}
