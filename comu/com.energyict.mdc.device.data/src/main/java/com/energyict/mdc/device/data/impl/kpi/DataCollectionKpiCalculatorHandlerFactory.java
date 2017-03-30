/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.kpi;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.tasks.TaskService;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskReportService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskReportService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

/**
 * Provides factory services for {@link DataCollectionKpiCalculatorHandler}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-07 (17:30)
 */
@Component(name="com.energyict.mdc.device.data.kpi.calculator.factory", service = MessageHandlerFactory.class, property = {"subscriber=" + DataCollectionKpiCalculatorHandlerFactory.TASK_SUBSCRIBER, "destination=" + DataCollectionKpiCalculatorHandlerFactory.TASK_DESTINATION}, immediate = true)
public class DataCollectionKpiCalculatorHandlerFactory implements MessageHandlerFactory {

    public static final String TASK_DESTINATION = "MDCKpiCalculatorTopic";
    public static final String TASK_SUBSCRIBER = "MDCKpiCalculator";
    public static final String TASK_SUBSCRIBER_DISPLAYNAME = "Calculate connection and communication kpi's";

    private volatile TaskService taskService;
    private volatile DataCollectionKpiService dataCollectionKpiService;
    private volatile ConnectionTaskReportService connectionTaskReportService;
    private volatile CommunicationTaskReportService communicationTaskReportService;

    // For OSGi framework only
    public DataCollectionKpiCalculatorHandlerFactory() {super();}

    // For unit testing purposes only
    @Inject
    public DataCollectionKpiCalculatorHandlerFactory(TaskService taskService, DataCollectionKpiService dataCollectionKpiService, ConnectionTaskReportService connectionTaskReportService, CommunicationTaskReportService communicationTaskReportService) {
        this();
        this.setTaskService(taskService);
        this.setDataCollectionKpiService(dataCollectionKpiService);
        this.setConnectionTaskReportService(connectionTaskReportService);
        this.setCommunicationTaskService(communicationTaskReportService);
    }

    @Override
    public MessageHandler newMessageHandler() {
        return this.taskService.createMessageHandler(
                new DataCollectionKpiCalculatorHandler(
                        this.dataCollectionKpiService,
                        this.connectionTaskReportService,
                        this.communicationTaskReportService));
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void setDataCollectionKpiService(DataCollectionKpiService dataCollectionKpiService) {
        this.dataCollectionKpiService = dataCollectionKpiService;
    }

    @Reference
    public void setConnectionTaskReportService(ConnectionTaskReportService connectionTaskReportService) {
        this.connectionTaskReportService = connectionTaskReportService;
    }

    @Reference
    public void setCommunicationTaskService(CommunicationTaskReportService communicationTaskReportService) {
        this.communicationTaskReportService = communicationTaskReportService;
    }

}