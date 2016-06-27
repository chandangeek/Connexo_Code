package com.elster.jupiter.validation.impl.kpi;

import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.validation.kpi.DataValidationKpiService;
import com.elster.jupiter.validation.kpi.DataValidationReportService;


public class DataManagementKpiCalculatorHandler implements TaskExecutor {

    private final DataValidationKpiService dataValidationKpiService;
    private final DataValidationReportService dataValidationReportService;

    public DataManagementKpiCalculatorHandler(DataValidationKpiService dataValidationKpiService, DataValidationReportService dataValidationReportService){
        this.dataValidationKpiService = dataValidationKpiService;
        this.dataValidationReportService = dataValidationReportService;
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

        @Override
        public DataValidationReportService dataValidationReportService() {
            return dataValidationReportService;
        }
    }

}
