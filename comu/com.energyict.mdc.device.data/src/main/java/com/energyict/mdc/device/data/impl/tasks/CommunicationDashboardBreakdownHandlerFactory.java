package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.tasks.TaskService;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;

@Component(name = "com.energyict.mdc.device.data.impl.tasks.CommunicationDashboardBreakdownHandlerFactory",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + CommunicationDashboardBreakdownHandlerFactory.COMM_DASHBOARD_BREAKDOWN_TASK_SUBSCRIBER,
                "destination=" + CommunicationDashboardBreakdownHandlerFactory.COMM_DASHBOARD_BREAKDOWN_TASK_DESTINATION},
        immediate = true)
public class CommunicationDashboardBreakdownHandlerFactory implements MessageHandlerFactory {

    public static final String COMM_DASHBOARD_BREAKDOWN_TASK_DESTINATION = "CommDshBreakdownTopic";
    public static final String COMM_DASHBOARD_BREAKDOWN_TASK_SUBSCRIBER = "CommDshBreakdownSubscriber";
    //CommDshBreakdownSubscriber
    public static final String COMM_DASHBOARD_BREAKDOWN_TASK_DISPLAYNAME = "Comm Count Breakdown";

    private volatile TaskService taskService;
    private volatile DeviceDataModelService deviceDataModelService;

    public CommunicationDashboardBreakdownHandlerFactory() {

    }

    @Inject
    CommunicationDashboardBreakdownHandlerFactory(TaskService taskService, NlsService nlsService, DeviceDataModelService deviceDataModelService) {
        setTaskService(taskService);
        setDeviceDataModelService(deviceDataModelService);
    }

    @Override
    public MessageHandler newMessageHandler() {
        return taskService.createMessageHandler(new CommunicationDashboardBreakdownHandler(deviceDataModelService));
    }

    @Reference
    public final void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void setDeviceDataModelService(DeviceDataModelService deviceDataModelService) {
        this.deviceDataModelService = deviceDataModelService;
    }

}
