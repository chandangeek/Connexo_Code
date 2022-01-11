/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.processes.keyrenewal.api.impl.eventhandler;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.pubsub.EventHandler;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFilter;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.LogLevel;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.MessagesTask;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import org.apache.commons.lang3.math.NumberUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

@Component(name = "com.energyict.mdc.processes.keyrenewal.eventhandler", service = Subscriber.class, immediate = true)
public class ComTaskExecEventHandler extends EventHandler<LocalEvent> {

    private final static String DEVICE_KEY_RENEWAL_SERVICE_CALL_HANDLER_NAME = "DeviceKeyRenewalServiceCallHandler";
    private final static String SCHEDULED_COMTASKEXECUTION_FAILED = "com/energyict/mdc/device/data/scheduledcomtaskexecution/FAILED";
    private final static String MANUAL_COMTASKEXECUTION_FAILED = "com/energyict/mdc/device/data/manualcomtaskexecution/FAILED";
    private ServiceCallService serviceCallService;
    private CommunicationTaskService communicationTaskService;

    @Inject
    public ComTaskExecEventHandler(ServiceCallService serviceCallService, CommunicationTaskService communicationTaskService) {
        super(LocalEvent.class);
        setServiceCallService(serviceCallService);
        setCommunicationTaskService(communicationTaskService);
    }

    public ComTaskExecEventHandler() {
        super(LocalEvent.class);
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public void setCommunicationTaskService(CommunicationTaskService communicationTaskService) {
        this.communicationTaskService = communicationTaskService;
    }

    @Override
    protected void onEvent(LocalEvent event, Object... eventDetails) {
        if (event.getType().getTopic().equals(MANUAL_COMTASKEXECUTION_FAILED) || event.getType().getTopic().equals(SCHEDULED_COMTASKEXECUTION_FAILED)) {
            processEvent(event, this::onFailure);
        }
    }

    private void onFailure(ComTaskExecution comTaskExecution) {
        if (forSecurityDeviceMessageCategory(comTaskExecution)) {
            Device device = comTaskExecution.getDevice();
            findServiceCallsLinkedTo(device).forEach(serviceCall -> failKeyRenewalDeviceMessage(serviceCall, device));
        }
    }

    private void failKeyRenewalDeviceMessage(ServiceCall serviceCall, Device device) {
        DeviceMessage renewKeyMessages = device.getMessages().stream()
                .filter(dm -> serviceCall.getId() == NumberUtils.toLong(dm.getTrackingId()))
                .filter(dm -> dm.getDeviceMessageId().dbValue() == DeviceMessageId.SECURITY_KEY_RENEWAL.dbValue())
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Unable to find key renewal device message for service call with id:" + serviceCall.getId()));

        if (!renewKeyMessages.getStatus().equals(DeviceMessageStatus.FAILED)) {
            renewKeyMessages.updateDeviceMessageStatus(DeviceMessageStatus.FAILED);
            serviceCall.log(LogLevel.FINE, String.format("Device message '%s'(id: %d, release date: %s) is marked failed",
                    renewKeyMessages.getSpecification().getName(), renewKeyMessages.getId(), renewKeyMessages.getReleaseDate()));
        }
    }

    private void processEvent(LocalEvent event, EventProcessor processor) {
        Object source = event.getSource();
        if (source instanceof ComTaskExecution) {
            ComTaskExecution comTaskExecution = (ComTaskExecution) source;
            processor.process(comTaskExecution);
        }
    }

    private boolean forSecurityDeviceMessageCategory(ComTaskExecution comTaskExecution) {
        return comTaskExecution.getComTask().getProtocolTasks().stream()
                .filter(task -> task instanceof MessagesTask)
                .map(task -> ((MessagesTask) task))
                .map(MessagesTask::getDeviceMessageCategories)
                .flatMap(List::stream)
                .anyMatch(deviceMessageCategory -> deviceMessageCategory.getId() == 8); // Using 8 for DeviceMessageCategories.SECURITY
    }

    private List<ServiceCall> findServiceCallsLinkedTo(Device device) {
        ServiceCallFilter filter = new ServiceCallFilter();
        filter.targetObjects.add(device);
        filter.states = Collections.singletonList(DefaultState.WAITING.name());
        filter.types = Collections.singletonList(DEVICE_KEY_RENEWAL_SERVICE_CALL_HANDLER_NAME);
        return serviceCallService.getServiceCallFinder(filter).find();
    }

    private interface EventProcessor {
        void process(ComTaskExecution source);
    }

}
