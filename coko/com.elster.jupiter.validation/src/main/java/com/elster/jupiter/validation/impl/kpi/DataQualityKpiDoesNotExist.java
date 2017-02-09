/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl.kpi;

import com.elster.jupiter.validation.kpi.DataValidationKpi;

import java.util.logging.Level;
import java.util.logging.Logger;

class DataQualityKpiDoesNotExist implements DataQualityKpiCalculator {

    private final Logger logger;
    private final String payLoad;

    DataQualityKpiDoesNotExist(Logger logger, String payLoad) {
        super();
        this.logger = logger;
        this.payLoad = payLoad;
    }

    @Override
    public void calculateAndStore() {
        this.logger.log(Level.SEVERE, "Payload '" + this.payLoad + "' does not contain the unique identifier of a " + DataValidationKpi.class.getSimpleName());
    }
}