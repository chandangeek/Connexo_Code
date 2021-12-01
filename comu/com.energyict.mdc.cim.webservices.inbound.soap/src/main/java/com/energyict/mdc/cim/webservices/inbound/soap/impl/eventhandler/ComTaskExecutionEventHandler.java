/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl.eventhandler;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFilter;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings.ChildGetMeterReadingsDomainExtension;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings.ComTaskExecutionServiceCallHandler;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings.DeviceMessageServiceCallHandler;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.LoadProfilesTask;
import com.energyict.mdc.common.tasks.MessagesTask;
import com.energyict.mdc.common.tasks.RegistersTask;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ComTaskExecutionEventHandler implements MessageHandler {

    private Clock clock;
    private ServiceCallService serviceCallService;
    private JsonService jsonService;
    private CommunicationTaskService communicationTaskService;


    public ComTaskExecutionEventHandler(Clock clock, ServiceCallService serviceCallService, JsonService jsonService, CommunicationTaskService communicationTaskService) {
        super();
        this.clock = clock;
        this.serviceCallService = serviceCallService;
        this.jsonService = jsonService;
        this.communicationTaskService = communicationTaskService;
    }


    @Override
    public void process(Message message) {
        Map<String, Object> messageProperties = this.jsonService.deserialize(message.getPayload(), Map.class);

        if (messageProperties.get("event.topics") != null) {
            String topic = messageProperties.get("event.topics").toString();

            if (topic.equals(EventType.MANUAL_COMTASKEXECUTION_COMPLETED.topic())
                    || topic.equals(EventType.SCHEDULED_COMTASKEXECUTION_COMPLETED.topic())) {
                processEvent(messageProperties, this::onComTaskCompleted);
            } else if (topic.equals(EventType.MANUAL_COMTASKEXECUTION_FAILED.topic())
                    || topic.equals(EventType.SCHEDULED_COMTASKEXECUTION_FAILED.topic())) {
                processEvent(messageProperties, this::onComTaskFailed);
            }
        }
    }

    private void onComTaskFailed(ComTaskExecution comTaskExecution) {
        String comTaskName = comTaskExecution.getComTask().getName();
        if (forLoadProfileOrRegisterReading(comTaskExecution)) {
            findServiceCallsLinkedTo(comTaskExecution.getDevice(), ComTaskExecutionServiceCallHandler.SERVICE_CALL_HANDLER_NAME)
                    .forEach(serviceCall -> handleForFailure(serviceCall, comTaskName));
        } else if (forLoadProfilesDeviceMessage(comTaskExecution)) {
            findServiceCallsLinkedTo(comTaskExecution.getDevice(), DeviceMessageServiceCallHandler.SERVICE_CALL_HANDLER_NAME)
                    .forEach(serviceCall -> handleForFailure(serviceCall, comTaskName));
        }
        // skipp all other comTaskExecutions
    }

    private void handleForFailure(ServiceCall serviceCall, String comTaskName) {
        ChildGetMeterReadingsDomainExtension domainExtension = serviceCall.getExtension(ChildGetMeterReadingsDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));

        Instant triggerDate = domainExtension.getTriggerDate();
        if (clock.instant().isAfter(triggerDate) && comTaskName != null && comTaskName.equals(domainExtension.getCommunicationTask())) {
            serviceCall.log(LogLevel.SEVERE, String.format("Communication task execution '%s'(trigger date: %s) is failed",
                    comTaskName, triggerDate.atZone(ZoneId.systemDefault())));
            serviceCall.requestTransition(DefaultState.ONGOING);
            serviceCall.requestTransition(DefaultState.FAILED);
        }
    }

    private void onComTaskCompleted(ComTaskExecution comTaskExecution) {
        String comTaskName = comTaskExecution.getComTask().getName();
        if (forLoadProfileOrRegisterReading(comTaskExecution)) {
            findServiceCallsLinkedTo(comTaskExecution.getDevice(), ComTaskExecutionServiceCallHandler.SERVICE_CALL_HANDLER_NAME)
                    .forEach(serviceCall -> handleForReading(serviceCall, comTaskName));
        }
        // skip all other comTaskExecutions
    }

    private void handleForReading(ServiceCall serviceCall, String comTaskName) {
        ChildGetMeterReadingsDomainExtension domainExtension = serviceCall.getExtension(ChildGetMeterReadingsDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));

        Instant triggerDate = domainExtension.getTriggerDate();
        if (clock.instant().isAfter(triggerDate) && comTaskName != null
                && comTaskName.equals(domainExtension.getCommunicationTask())) {
            serviceCall.log(LogLevel.FINE, String.format("Communication task execution '%s'(trigger date: %s) is completed",
                    comTaskName, triggerDate.atZone(ZoneId.systemDefault())));
            serviceCall.requestTransition(DefaultState.ONGOING);
            serviceCall.requestTransition(DefaultState.SUCCESSFUL);
        }
    }

    private boolean forLoadProfileOrRegisterReading(ComTaskExecution comTaskExecution) {
        return comTaskExecution.getComTask().getProtocolTasks().stream()
                .anyMatch(task -> task instanceof LoadProfilesTask || task instanceof RegistersTask);
    }

    private boolean forLoadProfilesDeviceMessage(ComTaskExecution comTaskExecution) {
        return comTaskExecution.getComTask().getProtocolTasks().stream()
                .filter(task -> task instanceof MessagesTask)
                .map(task -> ((MessagesTask) task))
                .map(MessagesTask::getDeviceMessageCategories)
                .flatMap(List::stream)
                .anyMatch(deviceMessageCategory -> deviceMessageCategory.getId() == 16);
    }

    private interface EventProcessor {
        void process(ComTaskExecution source);
    }

    private void processEvent(Map<String, Object> messageProperties, EventProcessor processor) {
        if (messageProperties.get("id") != null) {
            Optional<ComTaskExecution> comTaskExecution = communicationTaskService.findComTaskExecution(((Number) (messageProperties.get("id"))).longValue());
            if (comTaskExecution.isPresent()) {
                processor.process(comTaskExecution.get());
            }
        }
    }


    // subset of values from com.energyict.mdc.device.data.impl.EventType
    // duplicates here due to com.energyict.mdc.device.data.impl.* is private package
    public enum EventType {
        MANUAL_COMTASKEXECUTION_COMPLETED("manualcomtaskexecution/COMPLETED"),
        MANUAL_COMTASKEXECUTION_FAILED("manualcomtaskexecution/FAILED"),
        SCHEDULED_COMTASKEXECUTION_COMPLETED("scheduledcomtaskexecution/COMPLETED"),
        SCHEDULED_COMTASKEXECUTION_FAILED("scheduledcomtaskexecution/FAILED");
        String topic;
        private static final String NAMESPACE = "com/energyict/mdc/device/data/";

        EventType(String topic) {
            this.topic = topic;
        }

        public String topic() {
            return NAMESPACE + topic;
        }
    }

    private List<ServiceCall> findServiceCallsLinkedTo(Device device, String handlerName) {
        ServiceCallFilter filter = new ServiceCallFilter();
        filter.targetObjects.add(device);
        filter.states = Collections.singletonList(DefaultState.WAITING.name());
        filter.types = Collections.singletonList(handlerName);
        return serviceCallService.getServiceCallFinder(filter).find();
    }
}
