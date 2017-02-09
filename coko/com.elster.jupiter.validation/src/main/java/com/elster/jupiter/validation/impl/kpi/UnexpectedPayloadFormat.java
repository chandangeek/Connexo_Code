/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl.kpi;

import java.util.logging.Level;
import java.util.logging.Logger;

class UnexpectedPayloadFormat implements DataQualityKpiCalculator {
    private final Logger logger;
    private final String payLoad;

    UnexpectedPayloadFormat(Logger logger, String payLoad) {
        super();
        this.logger = logger;
        this.payLoad = payLoad;
    }

    @Override
    public void calculateAndStore() {
        this.logger.log(Level.SEVERE, "Payload '" + this.payLoad + "' is not of the expected format");
    }
}