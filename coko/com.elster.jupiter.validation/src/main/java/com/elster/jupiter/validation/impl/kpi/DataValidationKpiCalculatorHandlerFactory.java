package com.elster.jupiter.validation.impl.kpi;


import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.validation.kpi.DataValidationKpiService;
import com.elster.jupiter.validation.kpi.DataValidationReportService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name="com.elster.jupiter.validation.impl.kpi", service = MessageHandlerFactory.class, property = {"subscriber=" + DataValidationKpiCalculatorHandlerFactory.TASK_SUBSCRIBER, "destination=" + DataValidationKpiCalculatorHandlerFactory.TASK_DESTINATION}, immediate = true)
public class DataValidationKpiCalculatorHandlerFactory implements MessageHandlerFactory {

    public static final String TASK_DESTINATION = "ValKpiCalcTopic";
    public static final String TASK_SUBSCRIBER = "ValKpiCalc";
    public static final String TASK_SUBSCRIBER_DISPLAYNAME = "Calculate data validation kpi's";

    private volatile TaskService taskService;
    private volatile DataValidationKpiService dataValidationKpiService;
    private volatile DataValidationReportService dataValidationReportService;

    public DataValidationKpiCalculatorHandlerFactory() {super();}

    @Inject
    public DataValidationKpiCalculatorHandlerFactory(TaskService taskService, DataValidationKpiService dataValidationKpiService, DataValidationReportService dataValidationReportService) {
        this();
        this.setTaskService(taskService);
        this.setDataValidationKpiService(dataValidationKpiService);
    }

    @Override
    public MessageHandler newMessageHandler() {
        return this.taskService.createMessageHandler(
                new DataManagementKpiCalculatorHandler(
                        dataValidationKpiService,
                        dataValidationReportService
                ));
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void setDataValidationReportService(DataValidationReportService dataValidationReportService){
        this.dataValidationReportService = dataValidationReportService;
    }

    @Reference
    public void setDataValidationKpiService(DataValidationKpiService dataValidationKpiService) {
        this.dataValidationKpiService = dataValidationKpiService;
    }

}
