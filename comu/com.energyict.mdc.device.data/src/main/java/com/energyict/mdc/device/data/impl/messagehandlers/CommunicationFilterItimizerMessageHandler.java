/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.messagehandlers;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.QueueMessage;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecification;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecificationMessage;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionQueueMessage;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ItemizeCommunicationsFilterQueueMessage;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.TaskService;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Functions.asStream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * This message handler will trigger communications to rerun. Supports both a group of comTaskExecutions identified by a filter and an exhaustive list
 */
public class CommunicationFilterItimizerMessageHandler implements MessageHandler {

    private DeviceConfigurationService deviceConfigurationService;
    private CommunicationTaskService communicationTaskService;
    private MeteringGroupsService meteringGroupsService;
    private SchedulingService schedulingService;
    private MessageService messageService;
    private TaskService taskService;
    private JsonService jsonService;

    @Override
    public void process(Message message) {
        Optional<DestinationSpec> destinationSpec = messageService.getDestinationSpec(CommunicationTaskService.COMMUNICATION_RESCHEDULER_QUEUE_DESTINATION);
        if (destinationSpec.isPresent()) {
            ItemizeCommunicationsFilterQueueMessage filterQueueMessage = jsonService.deserialize(message.getPayload(), ItemizeCommunicationsFilterQueueMessage.class);
            ComTaskExecutionFilterSpecification comTaskExecutionFilterSpecification = buildFilterFromJsonQuery(filterQueueMessage.comTaskExecutionFilterSpecificationMessage);
            List<ComTaskExecution> comTaskExecutions = communicationTaskService.findComTaskExecutionsByFilter(comTaskExecutionFilterSpecification, 0, Integer.MAX_VALUE - 1);
            comTaskExecutions.stream().forEach(c -> processMessagePost(new ComTaskExecutionQueueMessage(c.getId(), filterQueueMessage.action), destinationSpec.get()));
        } else {
            // LOG failure
        }

    }

    private void processMessagePost(QueueMessage message, DestinationSpec destinationSpec) {
        String json = jsonService.serialize(message);
        destinationSpec.message(json).send();
    }


    @Override
    public void onMessageDelete(Message message) {

    }

    public MessageHandler init(CommunicationTaskService communicationTaskService, DeviceConfigurationService deviceConfigurationService,
                               MeteringGroupsService meteringGroupsService, MessageService messageService, JsonService jsonService,
                               SchedulingService schedulingService, TaskService taskService) {
        this.communicationTaskService = communicationTaskService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.meteringGroupsService = meteringGroupsService;
        this.messageService = messageService;
        this.jsonService = jsonService;
        this.schedulingService = schedulingService;
        this.taskService = taskService;
        return this;
    }

    private ComTaskExecutionFilterSpecification buildFilterFromJsonQuery(ComTaskExecutionFilterSpecificationMessage primitiveFilter) {
        ComTaskExecutionFilterSpecification filter = new ComTaskExecutionFilterSpecification();
        filter.taskStatuses = EnumSet.noneOf(TaskStatus.class);
        if (primitiveFilter.currentStates!=null) {
            filter.taskStatuses.addAll(primitiveFilter.currentStates.stream().map(TaskStatus::valueOf).collect(toList()));
        }

        filter.comSchedules = new HashSet<>();
        if (primitiveFilter.comSchedules!=null) {
            // already optimized
            for (ComSchedule comSchedule : schedulingService.getAllSchedules()) {
                filter.comSchedules.addAll(primitiveFilter.comSchedules.stream().
                        filter(scheduleId -> comSchedule.getId() == scheduleId).
                        map(scheduleId -> comSchedule).
                        collect(Collectors.toList()));
            }
        }

        if (primitiveFilter.comTasks !=null) {
            filter.comTasks = primitiveFilter.comTasks
                    .stream()
                    .map(taskService::findComTask)
                    .flatMap(asStream())
                    .collect(Collectors.toSet());
        }

        filter.latestResults = EnumSet.noneOf(CompletionCode.class);
        if (primitiveFilter.latestResults!=null) {
            filter.latestResults=primitiveFilter.latestResults.stream().map(CompletionCode::valueOf).collect(toSet());
        }

        filter.deviceTypes = new HashSet<>();
        if (primitiveFilter.deviceTypes!=null) {
            filter.deviceTypes.addAll(
                    primitiveFilter.deviceTypes.stream()
                            .map(deviceConfigurationService::findDeviceType)
                            .flatMap(asStream())
                            .collect(toList()));
        }

        filter.deviceGroups = new HashSet<>();
        if (primitiveFilter.deviceGroups!=null) {
            filter.deviceGroups.addAll(primitiveFilter.deviceGroups.stream().
                    map(meteringGroupsService::findEndDeviceGroup).
                    filter(java.util.Optional::isPresent).
                    map(java.util.Optional::get).
                    collect(toSet()));
        }

        if (primitiveFilter.startIntervalFrom!=null || primitiveFilter.startIntervalTo!=null) {
            filter.lastSessionStart = Interval.of(primitiveFilter.startIntervalFrom, primitiveFilter.startIntervalTo);
        }

        if (primitiveFilter.finishIntervalFrom!= null || primitiveFilter.finishIntervalTo!=null) {
            filter.lastSessionEnd = Interval.of(primitiveFilter.finishIntervalFrom, primitiveFilter.finishIntervalTo);
        }

        return filter;
    }
}
