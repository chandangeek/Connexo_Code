package com.energyict.mdc.device.data.impl.kpi;

import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.tasks.TaskService;
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

    private volatile TaskService taskService;
    private volatile DataCollectionKpiService dataCollectionKpiService;
    private volatile ConnectionTaskService connectionTaskService;
    private volatile CommunicationTaskService communicationTaskService;

    // For OSGi framework only
    public DataCollectionKpiCalculatorHandlerFactory() {super();}

    // For unit testing purposes only
    @Inject
    public DataCollectionKpiCalculatorHandlerFactory(TaskService taskService, DataCollectionKpiService dataCollectionKpiService, ConnectionTaskService connectionTaskService, CommunicationTaskService communicationTaskService) {
        super();
        this.setTaskService(taskService);
        this.setDataCollectionKpiService(dataCollectionKpiService);
        this.setConnectionTaskService(connectionTaskService);
        this.setCommunicationTaskService(communicationTaskService);
    }

    @Override
    public MessageHandler newMessageHandler() {
        return this.taskService.createMessageHandler(
                new DataCollectionKpiCalculatorHandler(
                        this.dataCollectionKpiService,
                        this.connectionTaskService,
                        this.communicationTaskService));
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
    public void setConnectionTaskService(ConnectionTaskService connectionTaskService) {
        this.connectionTaskService = connectionTaskService;
    }

    @Reference
    public void setCommunicationTaskService(CommunicationTaskService communicationTaskService) {
        this.communicationTaskService = communicationTaskService;
    }

}