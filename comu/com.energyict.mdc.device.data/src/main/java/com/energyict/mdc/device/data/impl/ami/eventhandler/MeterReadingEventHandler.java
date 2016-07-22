package com.energyict.mdc.device.data.impl.ami.eventhandler;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFilter;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.ami.servicecall.OnDemandReadServiceCallDomainExtension;
import com.energyict.mdc.device.data.impl.ami.servicecall.handlers.OnDemandReadServiceCallHandler;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class MeterReadingEventHandler implements MessageHandler {

    private final JsonService jsonService;
    private final ServiceCallService serviceCallService;
    private final DeviceService deviceService;

    MeterReadingEventHandler(JsonService jsonService, DeviceService deviceService, ServiceCallService serviceCallService) {
        super();
        this.jsonService = jsonService;
        this.serviceCallService = serviceCallService;
        this.deviceService = deviceService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void process(Message message) {
        Map<String, Object> messageProperties = this.jsonService.deserialize(message.getPayload(), Map.class);
        System.out.println(messageProperties.get("event.topics").toString());

        if (messageProperties.get("meterId") != null) {
            findServiceCallsLinkedTo(deviceService.findDeviceById(Long.valueOf(messageProperties.get("meterId")
                    .toString())).orElseThrow(IllegalStateException::new))
                    .forEach(serviceCall -> handle(serviceCall, messageProperties));
        }
    }

    private void handle(ServiceCall serviceCall, Map<String, Object> messageProperties) {
        OnDemandReadServiceCallDomainExtension extension = serviceCall.getExtension(OnDemandReadServiceCallDomainExtension.class)
                .orElseThrow(IllegalStateException::new);
        long successfulTasks = extension.getSuccessfulTasks().longValue();
        long completedTasks = extension.getCompletedTasks().longValue();
        long expectedTasks = extension.getExpectedTasks().longValue();

        Instant triggerDate = Instant.ofEpochMilli(extension.getTriggerDate().longValue());

        if (Instant.ofEpochMilli(Long.valueOf(messageProperties.get("timestamp").toString())).isAfter(triggerDate)) {
            if(EventType.METERREADING_CREATED.equals(messageProperties.get("event.topics"))){
                extension.setSuccessfulTasks(new BigDecimal(++successfulTasks));
            } else {
                extension.setCompletedTasks(new BigDecimal(++completedTasks));
            }
            extension.setExpectedTasks(new BigDecimal(++expectedTasks));
            System.out.println();
            serviceCall.update(extension);
        }

        if (completedTasks >= expectedTasks) {
            if (successfulTasks >= completedTasks  && serviceCall.canTransitionTo(DefaultState.SUCCESSFUL)) {
                serviceCall.requestTransition(DefaultState.SUCCESSFUL);
            } else if (serviceCall.canTransitionTo(DefaultState.FAILED)) {
                serviceCall.requestTransition(DefaultState.FAILED);
            }
        }
    }

    private List<ServiceCall> findServiceCallsLinkedTo(Device device) {
        ServiceCallFilter filter = new ServiceCallFilter();
        filter.targetObject = device;
        filter.states = Collections.singletonList(DefaultState.ONGOING.name());
        filter.types = Collections.singletonList(OnDemandReadServiceCallHandler.SERVICE_CALL_HANDLER_NAME);
        return serviceCallService.getServiceCallFinder(filter).find();
    }

}