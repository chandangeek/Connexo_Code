package com.elster.jupiter.validation.impl.kpi;

import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskStatus;
import com.elster.jupiter.validation.kpi.DataValidationKpiService;
import com.elster.jupiter.validation.kpi.DataValidationReportService;

import java.time.Clock;

class DataManagementKpiCalculatorHandler implements TaskExecutor {

    private final DataValidationKpiService dataValidationKpiService;
    private final DataValidationReportService dataValidationReportService;
    private final Clock clock;

    DataManagementKpiCalculatorHandler(DataValidationKpiService dataValidationKpiService, DataValidationReportService dataValidationReportService, Clock clock){
        this.dataValidationKpiService = dataValidationKpiService;
        this.dataValidationReportService = dataValidationReportService;
        this.clock = clock;
    }

    @Override
    public void execute(TaskOccurrence taskOccurrence) {
        if (taskOccurrence.getStatus().equals(TaskStatus.BUSY) && taskOccurrence.getRecurrentTask().getNextExecution() == null) {
            return;
        }
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

        @Override
        public Clock getClock() {
            return clock;
        }
    }

}