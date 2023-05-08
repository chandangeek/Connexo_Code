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
    private final DataModel dataModel;
    private final TaskService taskService;
    private final MessageService messageService;

    private final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;
    private static final String DASHBOARD_COUNT_BREAKDOWN_TASK_SCHEDULE = "0 0/3 * * * ?";
    private static final String DASHBOARD_COUNT_BREAKDOWN_TASK_NAME = "Comm DSH Count Breakdown Task";

    @Inject
    UpgraderV10_9_26(DataModel dataModel, TaskService taskService, MessageService messageService) {
        this.dataModel = dataModel;
        this.taskService = taskService;
        this.messageService = messageService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, version(10, 9, 26));
        execute(dataModel, removeJobOnCommTaskDashboardProcedure());
        execute(dataModel, removeJobOnConTaskDashboardProcedure());
        removeExistingMVConnectionDataTable();
        upgradeSubscriberSpecs();
    }

    private void removeExistingMVConnectionDataTable() {
        if (dataModel.doesTableExist("MV_CONNECTIONDATA")) {
            execute(dataModel, "truncate table MV_CONNECTIONDATA");
            execute(dataModel, "drop table MV_CONNECTIONDATA");
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
            createTask(DASHBOARD_COUNT_BREAKDOWN_TASK_NAME, DASHBOARD_COUNT_BREAKDOWN_TASK_SCHEDULE, queue);
        } else {
            boolean notSubscribedYet = destinationSpec.get()
                    .getSubscribers()
                    .stream()
                    .noneMatch(spec -> spec.getName().equals(subscriberKey.getKey()));
            if (notSubscribedYet) {
                destinationSpec.get().activate();
                destinationSpec.get().subscribe(subscriberKey, DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN);
                createTask(DASHBOARD_COUNT_BREAKDOWN_TASK_NAME, DASHBOARD_COUNT_BREAKDOWN_TASK_SCHEDULE, destinationSpec.get());
            }
        }
    }

    private void createTask(String name, String schedule, DestinationSpec destinationSpec) {
        taskService.newBuilder()
                .setApplication("MultiSense")
                .setName(name)
                .setScheduleExpressionString(schedule)
                .setDestination(destinationSpec)
                .setPayLoad("Communication Breakdown")
                .scheduleImmediately(true)
                .build();
    }
}
