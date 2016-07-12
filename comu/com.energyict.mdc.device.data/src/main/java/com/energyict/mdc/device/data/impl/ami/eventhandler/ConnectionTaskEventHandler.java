package com.energyict.mdc.device.data.impl.ami.eventhandler;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.ami.servicecall.OnDemandReadServiceCallDomainExtension;
import com.energyict.mdc.device.data.impl.ami.servicecall.handlers.OnDemandReadServiceCallHandler;

import org.osgi.service.event.EventConstants;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ConnectionTaskEventHandler implements MessageHandler {

    private final JsonService jsonService;
    private final ServiceCallService serviceCallService;
    private final DeviceService deviceService;

    ConnectionTaskEventHandler(JsonService jsonService, DeviceService deviceService, ServiceCallService serviceCallService) {
        super();
        this.jsonService = jsonService;
        this.serviceCallService = serviceCallService;
        this.deviceService = deviceService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void process(Message message) {
        Map<String, Object> messageProperties = this.jsonService.deserialize(message.getPayload(), Map.class);
        String topic = (String) messageProperties.get(EventConstants.EVENT_TOPIC);

        findServiceCallsLinkedTo(deviceService.findDeviceById(Long.valueOf(messageProperties.get("meterId")
                .toString())).orElseThrow(IllegalStateException::new))
                .forEach(serviceCall -> this.handle(serviceCall, messageProperties));
    }

    private void handle(ServiceCall serviceCall, Map<String, Object> messageProperties) {
        OnDemandReadServiceCallDomainExtension extension = serviceCall.getExtension(OnDemandReadServiceCallDomainExtension.class)
                .orElseThrow(IllegalStateException::new);
        long successfulTasks = extension.getSuccessfulTasks().longValue();
        long expectedTasks = extension.getExpectedTasks().longValue();

        extension.setSuccessfulTasks(new BigDecimal(++successfulTasks));
        serviceCall.update(extension);

        if (successfulTasks >= expectedTasks && serviceCall.canTransitionTo(DefaultState.SUCCESSFUL)){
                serviceCall.requestTransition(DefaultState.SUCCESSFUL);
        }
    }

    private List<ServiceCall> findServiceCallsLinkedTo(Device device) {
        Set<ServiceCall> serviceCalls = serviceCallService.findServiceCalls(device, EnumSet.of(DefaultState.ONGOING));
        return serviceCalls.stream().filter(this::serviceCallUsedForReadOperation).collect(Collectors.toList());
    }

    private boolean serviceCallUsedForReadOperation(ServiceCall serviceCall) {
        String typeName = serviceCall.getType().getName();
        return typeName.equals(OnDemandReadServiceCallHandler.SERVICE_CALL_HANDLER_NAME);
    }
}