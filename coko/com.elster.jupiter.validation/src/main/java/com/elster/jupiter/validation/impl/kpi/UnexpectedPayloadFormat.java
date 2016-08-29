package com.elster.jupiter.validation.impl.kpi;

import java.util.logging.Level;
import java.util.logging.Logger;

class UnexpectedPayloadFormat implements DataManagementKpiCalculator {
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