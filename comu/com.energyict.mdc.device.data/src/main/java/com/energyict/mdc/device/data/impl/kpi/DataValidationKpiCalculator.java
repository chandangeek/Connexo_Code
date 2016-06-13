package com.energyict.mdc.device.data.impl.kpi;

import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DataValidationKpiCalculator extends AbstractDataManagementKpiCalculatorImpl implements DataManagementKpiCalculator {

    private final DataValidationKpiImpl kpi;
    private final Logger logger;

    public DataValidationKpiCalculator(DataValidationKpiImpl dataValidationKpi, Instant timestamp, Logger logger) {
        super(timestamp);
        this.kpi = dataValidationKpi;
        this.logger = logger;
    }

    @Override
    public void calculateAndStore() {
        logger.log(Level.INFO, "++++++++++++++++++++++++++>>>>>>>>>>> CalculateAndStore !!!");
    }
}
