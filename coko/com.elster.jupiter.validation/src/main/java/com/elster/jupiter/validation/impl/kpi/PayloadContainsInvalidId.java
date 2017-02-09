/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl.kpi;

import com.elster.jupiter.metering.EndDevice;

import java.util.logging.Level;
import java.util.logging.Logger;

class PayloadContainsInvalidId implements DataManagementKpiCalculator {

    private final Logger logger;
    private final String payLoad;
    private final NumberFormatException exception;

    PayloadContainsInvalidId(Logger logger, String payLoad, NumberFormatException exception) {
        super();
        this.logger = logger;
        this.payLoad = payLoad;
        this.exception = exception;
    }

    @Override
    public void calculate() {
        this.logger.log(Level.SEVERE, "The data validation kpi identifier in the payload '" + this.payLoad + "' could not be parsed to long", this.exception);
    }

    @Override
    public void store(EndDevice endDevice) {
        calculate();
    }

}
