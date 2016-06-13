package com.elster.jupiter.validation.impl.kpi;

import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DataValidationKpiCalculator implements DataManagementKpiCalculator {

    private final DataValidationKpiImpl kpi;
    private final Logger logger;
    private final Instant timestamp;

    public DataValidationKpiCalculator(DataValidationKpiImpl dataValidationKpi, Instant timestamp, Logger logger) {
        this.timestamp = timestamp ;
        this.kpi = dataValidationKpi;
        this.logger = logger;
    }

    @Override
    public void calculateAndStore() {
        //FixMe implement calculateAndStore for DataValidationKpiCalculator
        logger.log(Level.INFO, "++++++++++++++++++++++++++>>>>>>>>>>> CalculateAndStore !!!");

    }
}
