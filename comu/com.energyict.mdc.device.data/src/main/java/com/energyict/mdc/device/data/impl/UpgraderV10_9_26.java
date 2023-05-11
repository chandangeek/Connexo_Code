/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.upgrade.Upgrader;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.impl.tasks.DashboardBreakdownHandlerFactory;

import javax.inject.Inject;
import java.util.Optional;

import static com.elster.jupiter.orm.Version.version;

public class UpgraderV10_9_26 implements Upgrader {
    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;

    private final DataModel dataModel;
    private final TaskService taskService;
    private final MessageService messageService;

    @Inject
    UpgraderV10_9_26(DataModel dataModel, TaskService taskService, MessageService messageService) {
        this.dataModel = dataModel;
        this.taskService = taskService;
        this.messageService = messageService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, version(10, 9, 26));
        execute(dataModel, removeJobOnCommTaskDashboardProcedure(), removeJobOnConTaskDashboardProcedure());
        removeExistingMVConnectionDataTable();
        upgradeSubscriberSpecs();
    }

    private void removeExistingMVConnectionDataTable() {
        if (dataModel.doesTableExist("MV_CONNECTIONDATA")) {
            execute(dataModel, "truncate table MV_CONNECTIONDATA", "drop table MV_CONNECTIONDATA");
        }
    }

    private String removeJobOnCommTaskDashboardProcedure() {
        return dataModel.getDropJobStatement("REF_COMTASK_DASHBOARD");
    }

    private String removeJobOnConTaskDashboardProcedure() {
        return dataModel.getDropJobStatement("REF_CONTASK_DASHBOARD");
    }

    private void upgradeSubscriberSpecs() {
        QueueTableSpec defaultQueueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        this.createMessageHandlerIfNotPresent(defaultQueueTableSpec, DashboardBreakdownHandlerFactory.DASHBOARD_BREAKDOWN_TASK_DESTINATION, SubscriberTranslationKeys.DASHBOARD_BREAKDOWN_TASK_SUBSCRIBER_NAME);
    }

    private void createMessageHandlerIfNotPresent(QueueTableSpec queueTableSpec, String destination, SubscriberTranslationKeys subscriberKey) {
        Optional<DestinationSpec> destinationSpec = messageService.getDestinationSpec(destination);
        if (!destinationSpec.isPresent()) {
            DestinationSpec queue = queueTableSpec.createDestinationSpec(destination, DEFAULT_RETRY_DELAY_IN_SECONDS);
            queue.activate();
            queue.subscribe(subscriberKey, DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN);
            createTask(DashboardBreakdownHandlerFactory.DASHBOARD_COUNT_BREAKDOWN_TASK_NAME, DashboardBreakdownHandlerFactory.DASHBOARD_COUNT_BREAKDOWN_TASK_SCHEDULE, queue);
        } else {
            boolean notSubscribedYet = destinationSpec.get()
                    .getSubscribers()
                    .stream()
                    .noneMatch(spec -> spec.getName().equals(subscriberKey.getKey()));
            if (notSubscribedYet) {
                destinationSpec.get().activate();
                destinationSpec.get().subscribe(subscriberKey, DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN);
                createTask(DashboardBreakdownHandlerFactory.DASHBOARD_COUNT_BREAKDOWN_TASK_NAME, DashboardBreakdownHandlerFactory.DASHBOARD_COUNT_BREAKDOWN_TASK_SCHEDULE, destinationSpec.get());
            }
        }
    }

    private void createTask(String name, String schedule, DestinationSpec destinationSpec) {
        taskService.newBuilder()
                .setApplication(DashboardBreakdownHandlerFactory.DASHBOARD_COUNT_BREAKDOWN_TASK_APPLICATION)
                .setName(name)
                .setScheduleExpressionString(schedule)
                .setDestination(destinationSpec)
                .setPayLoad(DashboardBreakdownHandlerFactory.DASHBOARD_COUNT_BREAKDOWN_TASK_PAYLOAD)
                .scheduleImmediately(true)
                .build();
    }
}
