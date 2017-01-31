/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.MessageSeeds;
import com.elster.jupiter.nls.Thesaurus;

import java.util.logging.Logger;

public class LoggingUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final Logger LOGGER = Logger.getLogger(LoggingUncaughtExceptionHandler.class.getName());

    private final Thesaurus thesaurus;

    LoggingUncaughtExceptionHandler(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        MessageSeeds.THREAD_UNCAUGHT_EXCEPTION.log(LOGGER, thesaurus, e, t.getName());
    }
}
