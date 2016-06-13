package com.elster.jupiter.validation.impl.kpi;

import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.validation.kpi.DataValidationKpiService;


public class DataManagementKpiCalculatorHandler implements TaskExecutor {

    private final DataValidationKpiService dataValidationKpiService;

    public DataManagementKpiCalculatorHandler(DataValidationKpiService dataValidationKpiService){
        this.dataValidationKpiService = dataValidationKpiService;
    }

    @Override
    public void execute(TaskOccurrence taskOccurrence) {
        KpiType.calculatorForRecurrentPayload(taskOccurrence, new ServiceProvider()).calculateAndStore();
    }

    private class ServiceProvider implements KpiType.ServiceProvider {

        @Override
        public DataValidationKpiService dataValidationKpiService() {
            return dataValidationKpiService;
        }
    }

}
