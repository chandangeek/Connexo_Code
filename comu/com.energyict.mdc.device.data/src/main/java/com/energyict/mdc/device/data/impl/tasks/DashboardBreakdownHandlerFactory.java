/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 *
 */
package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.tasks.TaskService;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "com.energyict.mdc.device.data.impl.tasks.CommunicationDashboardBreakdownHandlerFactory",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + DashboardBreakdownHandlerFactory.DASHBOARD_BREAKDOWN_TASK_SUBSCRIBER,
                "destination=" + DashboardBreakdownHandlerFactory.DASHBOARD_BREAKDOWN_TASK_DESTINATION},
        immediate = true)
public class DashboardBreakdownHandlerFactory implements MessageHandlerFactory {

    public static final String DASHBOARD_BREAKDOWN_TASK_DESTINATION = "DshBreakdownTopic";
    public static final String DASHBOARD_BREAKDOWN_TASK_SUBSCRIBER = "DshBreakdownSubscriber";
    public static final String COMM_DASHBOARD_BREAKDOWN_TASK_DISPLAYNAME = "Dashboard Count Breakdown";
    public static final String DASHBOARD_COUNT_BREAKDOWN_TASK_SCHEDULE = "0 0/3 * * * ?";
    public static final String DASHBOARD_COUNT_BREAKDOWN_TASK_NAME = "Communication breakdown task";

    private volatile TaskService taskService;
    private volatile DeviceDataModelService deviceDataModelService;

    public DashboardBreakdownHandlerFactory() {
        //For OSGI framework purpose
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
