/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl.calc;

import com.elster.jupiter.dataquality.DataQualityKpi;

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
        this.logger.log(Level.SEVERE, "Payload '" + this.payLoad + "' does not contain the unique identifier of a " + DataQualityKpi.class.getSimpleName());
    }
}
