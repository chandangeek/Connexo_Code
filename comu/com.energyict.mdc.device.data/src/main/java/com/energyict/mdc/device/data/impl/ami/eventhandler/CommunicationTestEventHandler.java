package com.energyict.mdc.device.data.impl.ami.eventhandler;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFilter;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommunicationTestServiceCallDomainExtension;
import com.energyict.mdc.device.data.impl.ami.servicecall.handlers.CommunicationTestServiceCallHandler;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CommunicationTestEventHandler implements MessageHandler {

    private final JsonService jsonService;
    private final ServiceCallService serviceCallService;
    private final DeviceService deviceService;
    private final MeteringService meteringService;

    CommunicationTestEventHandler(JsonService jsonService, DeviceService deviceService, ServiceCallService serviceCallService, MeteringService meteringService) {
        super();
        this.jsonService = jsonService;
        this.serviceCallService = serviceCallService;
        this.deviceService = deviceService;
        this.meteringService = meteringService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void process(Message message) {
        Map<String, Object> messageProperties = this.jsonService.deserialize(message.getPayload(), Map.class);

        if (messageProperties.get("meterId") != null) {
            findServiceCallsLinkedTo(meteringService
                    .findMeterById(Long.parseLong(messageProperties.get("meterId").toString()))
                    .flatMap(meter -> deviceService.findDeviceById(Long.parseLong(meter.getAmrId())))
                    .orElseThrow(IllegalStateException::new))
                    .forEach(serviceCall -> handle(serviceCall, messageProperties));
        } else if (messageProperties.get("deviceIdentifier") != null) {
            findServiceCallsLinkedTo(deviceService.findDeviceById(Long.parseLong(messageProperties.get("deviceIdentifier")
                    .toString())).orElseThrow(IllegalStateException::new))
                    .forEach(serviceCall -> handle(serviceCall, messageProperties));
        }
    }

    private void handle(ServiceCall serviceCall, Map<String, Object> messageProperties) {
        long id = serviceCall.getId();
        serviceCall = serviceCallService.lockServiceCall(id).orElseThrow(() -> new IllegalStateException("Unable to lock service call with id " + id));
        CommunicationTestServiceCallDomainExtension extension = serviceCall.getExtension(CommunicationTestServiceCallDomainExtension.class)
                .orElseThrow(IllegalStateException::new);
        long successfulTasks = extension.getSuccessfulTasks().longValue();
        long completedTasks = extension.getCompletedTasks().longValue();
        long expectedTasks = extension.getExpectedTasks().longValue();

        if (Instant.ofEpochMilli(Long.valueOf(messageProperties.get("timestamp").toString())).isAfter(extension.getTriggerDate())) {
            if ("com/energyict/mdc/connectiontask/COMPLETION".equals(messageProperties.get("event.topics"))) {
                if (messageProperties.get("failedTaskIDs").equals("") && messageProperties.get("skippedTaskIDs").equals("")) {
                    extension.setSuccessfulTasks(new BigDecimal(++successfulTasks));
                    extension.setCompletedTasks(new BigDecimal(++completedTasks));
                } else {
                    extension.setCompletedTasks(new BigDecimal(++completedTasks));
                }
            }
            serviceCall.update(extension);
        }

        if (completedTasks >= expectedTasks) {
            if (successfulTasks >= completedTasks && serviceCall.canTransitionTo(DefaultState.SUCCESSFUL)) {
                serviceCall.requestTransition(DefaultState.SUCCESSFUL);
            } else if (serviceCall.canTransitionTo(DefaultState.FAILED)) {
                serviceCall.requestTransition(DefaultState.FAILED);
            }
        }
    }

    private List<ServiceCall> findServiceCallsLinkedTo(Device device) {
        ServiceCallFilter filter = new ServiceCallFilter();
        filter.targetObjects.add(device);
        filter.states = Collections.singletonList(DefaultState.ONGOING.name());
        filter.types = Collections.singletonList(CommunicationTestServiceCallHandler.SERVICE_CALL_HANDLER_NAME);
        return serviceCallService.getServiceCallFinder(filter).find();
    }
}