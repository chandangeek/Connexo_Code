/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl.eventhandler;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.pubsub.EventHandler;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFilter;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings.ChildGetMeterReadingsDomainExtension;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings.ComTaskExecutionServiceCallHandler;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings.DeviceMessageServiceCallHandler;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.tasks.MessagesTask;
import com.energyict.mdc.tasks.RegistersTask;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Component(name = "com.energyict.mdc.cim.webservices.inbound.soap.eventhandler", service = Subscriber.class, immediate = true)
public class ComTaskExecutionEventHandler extends EventHandler<LocalEvent> {

    private Clock clock;
    private ServiceCallService serviceCallService;


    @Inject
    public ComTaskExecutionEventHandler( Clock clock) {
        super(LocalEvent.class);
        this.clock = clock;
    }

    public ComTaskExecutionEventHandler() {
        super(LocalEvent.class);
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    protected void onEvent(LocalEvent event, Object... eventDetails) {
        if (event.getType().getTopic().equals(EventType.MANUAL_COMTASKEXECUTION_COMPLETED.topic())
                || event.getType().getTopic().equals(EventType.SCHEDULED_COMTASKEXECUTION_COMPLETED.topic())) {
            processEvent(event, this::onComTaskCompleted);
        } else if (event.getType().getTopic().equals(EventType.MANUAL_COMTASKEXECUTION_FAILED.topic())
                || event.getType().getTopic().equals(EventType.SCHEDULED_COMTASKEXECUTION_FAILED.topic())) {
            processEvent(event, this::onComTaskFailed);
        }
    }

    private void onComTaskFailed(ComTaskExecution comTaskExecution) {
        if (forLoadProfileOrRegisterReading(comTaskExecution)) {
            findServiceCallsLinkedTo(comTaskExecution.getDevice(), ComTaskExecutionServiceCallHandler.SERVICE_CALL_HANDLER_NAME)
                    .forEach(serviceCall -> handleForFailure(serviceCall) );
        } else if (forLoadProfilesDeviceMessage(comTaskExecution)) {
            findServiceCallsLinkedTo(comTaskExecution.getDevice(), DeviceMessageServiceCallHandler.SERVICE_CALL_HANDLER_NAME)
                    .forEach(serviceCall -> handleForFailure(serviceCall));
        }
        // skipp all other comTaskExecutions
    }

    private void handleForFailure(ServiceCall serviceCall) {
        serviceCall.log(LogLevel.SEVERE, "Communication task execution is failed");
        serviceCall.requestTransition(DefaultState.ONGOING);
        serviceCall.requestTransition(DefaultState.FAILED);
    }

    private void onComTaskCompleted(ComTaskExecution comTaskExecution) {
        if (forLoadProfileOrRegisterReading(comTaskExecution)) {
            findServiceCallsLinkedTo(comTaskExecution.getDevice(), ComTaskExecutionServiceCallHandler.SERVICE_CALL_HANDLER_NAME)
                    .forEach(serviceCall -> handleForReading(serviceCall));
        } else if (forLoadProfilesDeviceMessage(comTaskExecution)) {
            findServiceCallsLinkedTo(comTaskExecution.getDevice(), DeviceMessageServiceCallHandler.SERVICE_CALL_HANDLER_NAME)
                    .forEach(serviceCall -> handleForDeviceMessages(serviceCall, comTaskExecution.getDevice()));
        }
        // skipp all other comTaskExecutions
    }

    private void handleForReading(ServiceCall serviceCall) {
        ChildGetMeterReadingsDomainExtension domainExtension = serviceCall.getExtension(ChildGetMeterReadingsDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));

        Instant triggerDate = domainExtension.getTriggerDate();
        if (clock.instant().isAfter(triggerDate)) {
            serviceCall.log(LogLevel.INFO, "Communication task execution is completed");
            serviceCall.requestTransition(DefaultState.ONGOING);
            serviceCall.requestTransition(DefaultState.SUCCESSFUL);
        }
    }

    private void handleForDeviceMessages(ServiceCall serviceCall, Device device) {
        ChildGetMeterReadingsDomainExtension domainExtension = serviceCall.getExtension(ChildGetMeterReadingsDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));

        Instant triggerDate = domainExtension.getTriggerDate();
        if (clock.instant().isAfter(triggerDate)) {
            if (device.getMessages().stream()
                    .map(DeviceMessage::getStatus)
                    .filter(deviceMessageStatus -> deviceMessageStatus.equals(DeviceMessageStatus.CONFIRMED))
                    .findAny()
                    .isPresent()) {
                serviceCall.requestTransition(DefaultState.ONGOING);
                serviceCall.log(LogLevel.INFO, "Device message is confirmed");
                serviceCall.requestTransition(DefaultState.SUCCESSFUL);
            } else {
                /// TODO check all cases != Confirmed ?
                serviceCall.requestTransition(DefaultState.ONGOING);
                serviceCall.log(LogLevel.SEVERE, "Device message wasn't confirmed");
                /// FIXME it is temporal workaround
                serviceCall.requestTransition(DefaultState.SUCCESSFUL);
//                serviceCall.requestTransition(DefaultState.FAILED);
            }
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

    private void processEvent(LocalEvent event, EventProcessor processor) {
        Object source = event.getSource();
        if (source instanceof ComTaskExecution) {
            ComTaskExecution comTaskExecution = (ComTaskExecution) source;
            processor.process(comTaskExecution);
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
        filter.targetObject = device;
        filter.states = Collections.singletonList(DefaultState.WAITING.name());
        filter.types = Collections.singletonList(handlerName);
        return serviceCallService.getServiceCallFinder(filter).find();
    }
}
