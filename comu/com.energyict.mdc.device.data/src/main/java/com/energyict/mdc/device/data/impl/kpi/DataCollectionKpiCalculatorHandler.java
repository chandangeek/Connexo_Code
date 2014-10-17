package com.energyict.mdc.device.data.impl.kpi;

import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;

import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;

/**
 * Calculates the scores for a {@link DataCollectionKpiImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-07 (16:50)
 */
public class DataCollectionKpiCalculatorHandler implements TaskExecutor {


    private final DataCollectionKpiService dataCollectionKpiService;
    private final ConnectionTaskService connectionTaskService;
    private final CommunicationTaskService communicationTaskService;

    public DataCollectionKpiCalculatorHandler(DataCollectionKpiService dataCollectionKpiService, ConnectionTaskService connectionTaskService, CommunicationTaskService communicationTaskService) {
        super();
        this.dataCollectionKpiService = dataCollectionKpiService;
        this.connectionTaskService = connectionTaskService;
        this.communicationTaskService = communicationTaskService;
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
        public ConnectionTaskService connectionTaskService() {
            return connectionTaskService;
        }

        @Override
        public CommunicationTaskService communicationTaskService() {
            return communicationTaskService;
        }
    }

}