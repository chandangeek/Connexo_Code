/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl.eventhandler;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFilter;
import com.elster.jupiter.servicecall.ServiceCallService;

import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings.ChildGetMeterReadingsDomainExtension;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings.DeviceMessageServiceCallHandler;
import com.energyict.mdc.common.protocol.DeviceMessage;

import org.apache.commons.lang3.math.NumberUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Optional;

@Component(name = "com.energyict.mdc.cim.webservices.inbound.soap.devicemessage.update.eventhandler", service = TopicHandler.class, immediate = true)
public class DeviceMessageUpdateForLoadProfileEventHandler implements TopicHandler {

    private static final String TOPIC = "com/energyict/mdc/device/data/deviceMessage/UPDATED";
    private volatile ServiceCallService serviceCallService;
    private volatile Clock clock;

    // For OSGi purpose
    public DeviceMessageUpdateForLoadProfileEventHandler() {
    }

    @Inject
    public DeviceMessageUpdateForLoadProfileEventHandler(ServiceCallService serviceCallService, Clock clock) {
        setServiceCallService(serviceCallService);
        setClock(clock);
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
    public String getTopicMatcher() {
        return TOPIC;
    }

    @Override
    public void handle(LocalEvent event) {
        DeviceMessage deviceMessage = (DeviceMessage) event.getSource();
        Optional<ServiceCall> serviceCallOptional = findDeviceMessageServiceCall(deviceMessage);
        if (!serviceCallOptional.isPresent()) {
            return;
        }
        ServiceCall serviceCall = serviceCallOptional.get();
        switch (deviceMessage.getStatus()) {
            case CONFIRMED:
                if (serviceCall.getState().isOpen()) {
                    serviceCall.requestTransition(DefaultState.ONGOING);
                    serviceCall.log(LogLevel.FINE, String.format("Device message '%s'(id: %d, release date: %s) is confirmed",
                            deviceMessage.getSpecification().getName(),
                            deviceMessage.getId(),
                            deviceMessage.getReleaseDate().atZone(ZoneId.systemDefault())));
                    serviceCall.requestTransition(DefaultState.SUCCESSFUL);
                }
                break;
            case CANCELED:
                if (serviceCall.getState().isOpen()) {
                    serviceCall.requestTransition(DefaultState.ONGOING);
                    serviceCall.log(LogLevel.FINE, String.format("Device message '%s'(id: %d, release date: %s) is canceled",
                            deviceMessage.getSpecification().getName(),
                            deviceMessage.getId(),
                            deviceMessage.getReleaseDate().atZone(ZoneId.systemDefault())));
                    serviceCall.requestTransition(DefaultState.CANCELLED);
                }
                break;
            case WAITING:
                // Intentional fall-through
            case SENT:
                // Intentional fall-through
            case PENDING:
                serviceCall.log(LogLevel.FINE, String.format("Device message '%s'(id: %d, release date: %s) is %s",
                        deviceMessage.getSpecification().getName(),
                        deviceMessage.getId(),
                        deviceMessage.getReleaseDate().atZone(ZoneId.systemDefault()),
                        deviceMessage.getStatus().toString()));
                break;
            case INDOUBT:
                // Intentional fall-through
            case FAILED:
                // Intentional fall-through
            default:
                serviceCall.requestTransition(DefaultState.ONGOING);
                serviceCall.log(LogLevel.SEVERE, String.format("Device message '%s'(id: %d, release date: %s) wasn't confirmed",
                        deviceMessage.getSpecification().getName(),
                        deviceMessage.getId(),
                        deviceMessage.getReleaseDate().atZone(ZoneId.systemDefault())));
                serviceCall.requestTransition(DefaultState.FAILED);
        }
    }

    private Optional<ServiceCall> findDeviceMessageServiceCall(DeviceMessage deviceMessage) {
        ServiceCallFilter filter = new ServiceCallFilter();
        filter.targetObjects.add(deviceMessage.getDevice());
        filter.states = Collections.singletonList(DefaultState.WAITING.name());
        filter.types = Collections.singletonList(DeviceMessageServiceCallHandler.SERVICE_CALL_HANDLER_NAME);
        return serviceCallService.getServiceCallFinder(filter).find().stream()
                .filter(sc -> sc.getId() == NumberUtils.toLong(deviceMessage.getTrackingId()))
                .filter(sc -> clock.instant().isAfter(sc.getExtension(ChildGetMeterReadingsDomainExtension.class).get().getTriggerDate()))
                .findFirst();

    }
}
