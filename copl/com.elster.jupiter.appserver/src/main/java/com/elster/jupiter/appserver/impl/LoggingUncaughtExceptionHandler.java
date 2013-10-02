package com.elster.jupiter.appserver.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final Logger logger = Logger.getLogger(LoggingUncaughtExceptionHandler.class.getName());

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        logger.log(Level.SEVERE, "Uncaught exception occurred on thread " + t.getName(), e);
    }
}
