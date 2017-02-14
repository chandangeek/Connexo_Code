/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl.kpi;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.validation.kpi.DataValidationKpi;

import java.util.logging.Level;
import java.util.logging.Logger;

class DataManagementKpiDoesNotExist implements DataManagementKpiCalculator {

    private final Logger logger;
    private final String payLoad;

    DataManagementKpiDoesNotExist(Logger logger, String payLoad) {
        super();
        this.logger = logger;
        this.payLoad = payLoad;
    }

    @Override
    public void calculate() {
        this.logger.log(Level.SEVERE, "Payload '" + this.payLoad + "' does not contain the unique identifier of a " + DataValidationKpi.class.getSimpleName());
    }

    @Override
    public void store(EndDevice endDevice) {
        calculate();
    }

}