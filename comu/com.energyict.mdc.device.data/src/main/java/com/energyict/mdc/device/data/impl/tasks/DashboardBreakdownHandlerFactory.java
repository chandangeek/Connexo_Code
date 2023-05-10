/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 *
 */
package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.tasks.TaskService;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;

import javax.inject.Inject;

public class DashboardBreakdownHandlerFactory implements MessageHandlerFactory {
    public static final String DASHBOARD_BREAKDOWN_TASK_DESTINATION = "DshBreakdownTopic";
    public static final String DASHBOARD_BREAKDOWN_TASK_SUBSCRIBER = "DshBreakdownSubscriber";
    public static final String DASHBOARD_BREAKDOWN_TASK_SUBSCRIBER_DISPLAYNAME = "Communication breakdown";
    public static final String DASHBOARD_COUNT_BREAKDOWN_TASK_SCHEDULE = "0 0/3 * * * ?"; // each 3 minutes
    public static final String DASHBOARD_COUNT_BREAKDOWN_TASK_NAME = "Communication breakdown task";
    public static final String DASHBOARD_COUNT_BREAKDOWN_TASK_APPLICATION = "MultiSense";
    public static final String DASHBOARD_COUNT_BREAKDOWN_TASK_PAYLOAD = "Communication breakdown";

    private final TaskService taskService;
    private final DeviceDataModelService deviceDataModelService;
    private final OrmService ormService;

    @Inject
    public DashboardBreakdownHandlerFactory(DeviceDataModelService deviceDataModelService, OrmService ormService, TaskService taskService) {
        this.deviceDataModelService = deviceDataModelService;
        this.ormService = ormService;
        this.taskService = taskService;
    }

    @Override
    public MessageHandler newMessageHandler() {
        return taskService.createMessageHandler(new DashboardBreakdownHandler(deviceDataModelService, ormService));
    }
}
