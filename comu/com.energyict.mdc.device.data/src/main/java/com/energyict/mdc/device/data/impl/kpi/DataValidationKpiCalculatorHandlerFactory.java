package com.energyict.mdc.device.data.impl.kpi;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.tasks.TaskService;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
import com.energyict.mdc.device.data.kpi.DataValidationKpiService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskReportService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskReportService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name="com.energyict.mdc.device.data.kpi.calculator.impl.factory", service = MessageHandlerFactory.class, property = {"subscriber=" + DataValidationKpiCalculatorHandlerFactory.TASK_SUBSCRIBER, "destination=" + DataValidationKpiCalculatorHandlerFactory.TASK_DESTINATION}, immediate = true)
public class DataValidationKpiCalculatorHandlerFactory implements MessageHandlerFactory {

    public static final String TASK_DESTINATION = "MDCValKpiCalcTopic";
    public static final String TASK_SUBSCRIBER = "MDCValKpiCalc";
    public static final String TASK_SUBSCRIBER_DISPLAYNAME = "Calculate data validation kpi's";
    private volatile TaskService taskService;
    private volatile DataCollectionKpiService dataCollectionKpiService;
    private volatile DataValidationKpiService dataValidationKpiService;
    private volatile ConnectionTaskReportService connectionTaskReportService;
    private volatile CommunicationTaskReportService communicationTaskReportService;

    public DataValidationKpiCalculatorHandlerFactory() {super();}

    @Inject
    public DataValidationKpiCalculatorHandlerFactory(TaskService taskService, DataCollectionKpiService dataCollectionKpiService, ConnectionTaskReportService connectionTaskReportService, CommunicationTaskReportService communicationTaskReportService, DataValidationKpiService dataValidationKpiService) {
        this();
        this.setTaskService(taskService);
        this.setDataCollectionKpiService(dataCollectionKpiService);
        this.setConnectionTaskReportService(connectionTaskReportService);
        this.setCommunicationTaskService(communicationTaskReportService);
        this.setDataValidationKpiService(dataValidationKpiService);
    }

    @Override
    public MessageHandler newMessageHandler() {
        return this.taskService.createMessageHandler(
                new DataManagementKpiCalculatorHandler(
                        this.dataCollectionKpiService,
                        this.connectionTaskReportService,
                        this.communicationTaskReportService,
                        this.dataValidationKpiService));
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
    public void setDataValidationKpiService(DataValidationKpiService dataValidationKpiService) {
        this.dataValidationKpiService = dataValidationKpiService;
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
