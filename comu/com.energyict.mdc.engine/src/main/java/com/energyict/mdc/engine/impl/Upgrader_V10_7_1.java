/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;

public class Upgrader_V10_7_1 implements Upgrader {

    public static final String COM_SERVER_ALIVE_TASK_DESTINATION = "ComServerStatusTopic";
    private static final String COM_SERVER_STATUS_TASK_NAME = "ComServerAliveStatusTask";
    private final MessageService messageService;
    private final TaskService taskService;

    @Inject
    public Upgrader_V10_7_1(MessageService messageService, TaskService taskService) {
        this.messageService = messageService;
        this.taskService = taskService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        messageService.getDestinationSpec(COM_SERVER_ALIVE_TASK_DESTINATION).ifPresent(DestinationSpec::delete);
        taskService.getRecurrentTask(COM_SERVER_STATUS_TASK_NAME).ifPresent(RecurrentTask::delete);
    }
}
