package com.elster.jupiter.bootstrap.logging.impl;

import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogService;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomOsgiLogListener implements LogListener {
    private static final Logger LOGGER = Logger.getLogger(CustomOsgiLogListener.class.getName());

    CustomOsgiLogListener() {
    }

    @Override
    public void logged(final LogEntry entry) {
        Level level;
        switch (entry.getLevel()) {
            case LogService.LOG_DEBUG: {
                level = Level.FINE;
                break;
            }
            case LogService.LOG_ERROR: {
                level = Level.SEVERE;
                break;
            }
            case LogService.LOG_INFO: {
                level = Level.INFO;
                break;
            }
            default: {
                level = Level.WARNING;
                break;
            }
        }
        Throwable ex = entry.getException();
        String name = entry.getBundle().getSymbolicName();
        String message = entry.getMessage();

        if (ex != null) {
            LOGGER.log(level, name + ": " + message, ex);
        } else {
            LOGGER.log(level, name + ": " + message);
        }
    }
}
