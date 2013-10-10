package com.elster.jupiter.appserver.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final Logger LOGGER = Logger.getLogger(LoggingUncaughtExceptionHandler.class.getName());

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        LOGGER.log(Level.SEVERE, "Uncaught exception occurred on thread " + t.getName(), e);
    }
}
