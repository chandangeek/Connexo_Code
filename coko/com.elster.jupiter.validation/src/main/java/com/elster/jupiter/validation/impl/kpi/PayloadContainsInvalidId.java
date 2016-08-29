package com.elster.jupiter.validation.impl.kpi;

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
    public void calculateAndStore() {
        this.logger.log(Level.SEVERE, "The data validation kpi identifier in the payload '" + this.payLoad + "' could not be parsed to long", this.exception);
    }

}
