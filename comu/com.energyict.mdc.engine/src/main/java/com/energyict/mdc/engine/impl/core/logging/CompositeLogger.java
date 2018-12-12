/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.logging;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Acts as a composite for {@link Logger}s,
 * effectively delegating all calls to the sub loggers.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-01-11 (14:19)
 */
public class CompositeLogger extends Logger {

    private List<Logger> loggers = new ArrayList<>();

    public CompositeLogger () {
        super(CompositeLogger.class.getPackage().getName(), null);
        this.setLevel(Level.ALL);
    }

    public void add (Logger logger) {
        this.loggers.add(logger);
    }

    @Override
    public void log (LogRecord record) {
        for (Logger logger : this.loggers) {
            logger.log(record);
        }
    }

    @Override
    public void setLevel (Level newLevel) throws SecurityException {
        /* Ignore this to avoid that some LogRecords
         * would not get forwarded to sub loggers. */
    }

}