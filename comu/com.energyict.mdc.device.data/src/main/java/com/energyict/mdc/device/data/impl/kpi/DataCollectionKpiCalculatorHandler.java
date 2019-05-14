/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.kpi;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskReportService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskReportService;

/**
 * Calculates the scores for a {@link DataCollectionKpiImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-07 (16:50)
 */
public class DataCollectionKpiCalculatorHandler implements TaskExecutor {


    private final DataCollectionKpiService dataCollectionKpiService;
    private final ConnectionTaskReportService connectionTaskReportService;
    private final CommunicationTaskReportService communicationTaskReportService;
    private final EventService eventService;

    public DataCollectionKpiCalculatorHandler(DataCollectionKpiService dataCollectionKpiService, ConnectionTaskReportService connectionTaskReportService, CommunicationTaskReportService communicationTaskReportService, EventService eventService) {
        super();
        this.dataCollectionKpiService = dataCollectionKpiService;
        this.connectionTaskReportService = connectionTaskReportService;
        this.communicationTaskReportService = communicationTaskReportService;
        this.eventService = eventService;
    }

    @Override
    public void execute(TaskOccurrence taskOccurrence) {
        try{
            KpiType.calculatorForRecurrentPayload(taskOccurrence, new ServiceProvider()).calculateAndStore();
        } catch (Exception e){
            postFailEvent(eventService, taskOccurrence, e.getLocalizedMessage());
            throw e;
        }

    }

    private class ServiceProvider implements KpiType.ServiceProvider {
        @Override
        public DataCollectionKpiService dataCollectionKpiService() {
            return dataCollectionKpiService;
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