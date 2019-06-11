/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.autoreschedule.impl;

import com.elster.jupiter.customtask.CustomTask;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.sql.Fetcher;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Helper class to provide the parameters needed by {@link com.energyict.mdc.autoreschedule.impl.FailedComTasksTrigger}
 */
class TaskParametersProvider {
    private static final Logger LOGGER = Logger.getLogger(TaskParametersProvider.class.getName());

    private final MeteringGroupsService meteringGroupsService;
    private final CommunicationTaskService communicationTaskService;

    private final TransactionService transactionService;
    private final Clock clock;
    private final CustomTask customTask;

    TaskParametersProvider(MeteringGroupsService meteringGroupsService, CommunicationTaskService communicationTaskService, TransactionService transactionService, Clock clock, CustomTask customTask) {
        this.meteringGroupsService = meteringGroupsService;
        this.communicationTaskService = communicationTaskService;
        this.transactionService = transactionService;
        this.clock = clock;
        this.customTask = customTask;
    }

    String getEndDeviceGroupName() {
        return customTask.getValues().get(TranslationKeys.END_DEVICE_GROUP_SELECTOR.getKey()).toString();
    }

    Fetcher<ComTaskExecution> getComTaskExecutionsForDevicesByComTask() {
        return communicationTaskService.findComTaskExecutionsForDevicesByComTask(getEndDeviceGroupMemberIds(), getComTaskIds());
    }

    int getTaskInterval() {
        int interval = RecurrenceParser.getSeconds(customTask.getScheduleExpression().encoded());
        LOGGER.info("AutoReschedule task encoded schedule expression: " + customTask.getScheduleExpression().encoded() +
                " corresponding interval in sec: " + interval);
        return RecurrenceParser.getSeconds(customTask.getScheduleExpression().encoded());
    }

    TransactionService getTransactionService() {
        return transactionService;
    }

    private List<Long> getEndDeviceGroupMemberIds() {
        Optional<EndDeviceGroup> endDeviceGroup = meteringGroupsService.findEndDeviceGroupByName(getEndDeviceGroupName());
        return endDeviceGroup.map(group -> group.getMembers(clock.instant()).stream()
                .map(endDevice -> endDevice.getId())
                .collect(Collectors.toList())).orElseGet(ArrayList::new);
    }

    private List<Long> getComTaskIds() {
        List<AutoRescheduleTaskFactory.ComTaskInfo> comTaskInfos = (List) customTask.getValues().get(TranslationKeys.COMTASK_SELECTOR.getKey());
        return comTaskInfos.stream()
                .map(AutoRescheduleTaskFactory.ComTaskInfo::getId)
                .collect(Collectors.toList());
    }
}

