package com.energyict.mdc.device.data.impl.kpi;

import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
import com.energyict.mdc.device.data.kpi.DataValidationKpiService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskReportService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskReportService;

/**
 * Calculates the scores for a {@link DataCollectionKpiImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-07 (16:50)
 */
public class DataManagementKpiCalculatorHandler implements TaskExecutor {


    private final DataCollectionKpiService dataCollectionKpiService;
    private final DataValidationKpiService dataValidationKpiService;
    private final ConnectionTaskReportService connectionTaskReportService;
    private final CommunicationTaskReportService communicationTaskReportService;

    public DataManagementKpiCalculatorHandler(DataCollectionKpiService dataCollectionKpiService, ConnectionTaskReportService connectionTaskReportService,
                                              CommunicationTaskReportService communicationTaskReportService, DataValidationKpiService dataValidationKpiService) {
        super();
        this.dataCollectionKpiService = dataCollectionKpiService;
        this.connectionTaskReportService = connectionTaskReportService;
        this.communicationTaskReportService = communicationTaskReportService;
        this.dataValidationKpiService = dataValidationKpiService;
    }

    @Override
    public void execute(TaskOccurrence taskOccurrence) {
        KpiType.calculatorForRecurrentPayload(taskOccurrence, new ServiceProvider()).calculateAndStore();
    }

    private class ServiceProvider implements KpiType.ServiceProvider {
        @Override
        public DataCollectionKpiService dataCollectionKpiService() {
            return dataCollectionKpiService;
        }

        @Override
        public DataValidationKpiService dataValidationKpiService() {
            return dataValidationKpiService;
        }

        @Override
        public ConnectionTaskReportService connectionTaskReportService() {
            return connectionTaskReportService;
        }

        @Override
        public CommunicationTaskReportService communicationTaskService() {
            return communicationTaskReportService;
        }
    }

}