/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Provides access to java.util.Logger.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-04-06 (11:09)
 */
enum Loggers {
    /**
     * Used during the analyis of the data aggregation request.
     */
    ANALYSIS,

    /**
     * Used during the sql generation phase of the data aggregation request.
     */
    SQL;

    private final Logger logger;

    Loggers() {
        this.logger = Logger.getLogger("com.elster.jupiter.metering.impl.aggregation." + this.name().toLowerCase());
    }

    void severe(Supplier<String> msgSupplier) {
        this.logger.severe(msgSupplier);
    }

    void warning(Supplier<String> msgSupplier) {
        this.logger.warning(msgSupplier);
    }

    void info(Supplier<String> msgSupplier) {
        this.logger.info(msgSupplier);
    }

    void debug(Supplier<String> msgSupplier) {
        this.logger.fine(msgSupplier);
    }

}