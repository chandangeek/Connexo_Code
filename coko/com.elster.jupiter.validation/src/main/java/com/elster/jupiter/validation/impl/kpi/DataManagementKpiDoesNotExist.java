package com.elster.jupiter.validation.impl.kpi;


import com.elster.jupiter.validation.kpi.DataValidationKpi;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DataManagementKpiDoesNotExist  implements DataManagementKpiCalculator{

    private final Logger logger;
    private final String payLoad;

    public DataManagementKpiDoesNotExist(Logger logger, String payLoad) {
        super();
        this.logger = logger;
        this.payLoad = payLoad;
    }

    @Override
    public void calculateAndStore() {
        this.logger.log(Level.SEVERE, "Payload '" + this.payLoad + "' does not contain the unique identifier of a " + DataValidationKpi.class.getSimpleName());
    }

}
