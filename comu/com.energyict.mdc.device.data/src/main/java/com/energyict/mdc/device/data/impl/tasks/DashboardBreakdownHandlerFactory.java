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
        property = {"subscriber=" + DashboardBreakdownHandlerFactory.DASHBOARD_BREAKDOWN_TASK_SUBSCRIBER,
                "destination=" + DashboardBreakdownHandlerFactory.DASHBOARD_BREAKDOWN_TASK_DESTINATION},
        immediate = true)
public class DashboardBreakdownHandlerFactory implements MessageHandlerFactory {

    public static final String DASHBOARD_BREAKDOWN_TASK_DESTINATION = "DshBreakdownTopic";
    public static final String DASHBOARD_BREAKDOWN_TASK_SUBSCRIBER = "DshBreakdownSubscriber";
    public static final String COMM_DASHBOARD_BREAKDOWN_TASK_DISPLAYNAME = "Dashboard Count Breakdown";

    private volatile TaskService taskService;
    private volatile DeviceDataModelService deviceDataModelService;

    public DashboardBreakdownHandlerFactory() {

    }

    @Inject
    DashboardBreakdownHandlerFactory(TaskService taskService, DeviceDataModelService deviceDataModelService) {
        setTaskService(taskService);
        setDeviceDataModelService(deviceDataModelService);
    }

    @Override
    public MessageHandler newMessageHandler() {
        return taskService.createMessageHandler(new DashboardBreakdownHandler(deviceDataModelService));
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
