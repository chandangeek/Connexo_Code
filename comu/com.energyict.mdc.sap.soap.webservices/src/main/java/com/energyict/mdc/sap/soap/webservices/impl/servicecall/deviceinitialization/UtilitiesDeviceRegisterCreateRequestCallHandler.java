/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;

import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;

import com.energyict.obis.ObisCode;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component(name = UtilitiesDeviceRegisterCreateRequestCallHandler.NAME, service = ServiceCallHandler.class,
        property = "name=" + UtilitiesDeviceRegisterCreateRequestCallHandler.NAME, immediate = true)
public class UtilitiesDeviceRegisterCreateRequestCallHandler implements ServiceCallHandler {

    public static final String NAME = "UtilitiesDeviceRegisterCreateRequestCallHandler";
    public static final String VERSION = "v1.0";

    private volatile SAPCustomPropertySets sapCustomPropertySets;


    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        switch (newState) {
            case PENDING:
                serviceCall.requestTransition(DefaultState.ONGOING);
                break;
            case ONGOING:
                processServiceCall(serviceCall);
                break;
            case CANCELLED:
                cancelServiceCall(serviceCall);
                break;
            default:
                // No specific action required for these states
                break;
        }
    }

    private void processServiceCall(ServiceCall serviceCall) {
        UtilitiesDeviceRegisterCreateRequestDomainExtension extension = serviceCall.getExtensionFor(new UtilitiesDeviceRegisterCreateRequestCustomPropertySet()).get();

        Optional<Device> device = sapCustomPropertySets.getDevice(extension.getDeviceId());
        if (device.isPresent()) {
            String interval = extension.getInterval();
            String obis = extension.getObis();

            if (interval == null || interval.equals("0")) {
                Optional<Register> register = device.get().getRegisterWithDeviceObisCode(ObisCode.fromString(obis));
                if (register.isPresent()) {
                    try {
                        sapCustomPropertySets.setLrn(register.get(), extension.getLrn(), extension.getStartDate(), extension.getEndDate());
                    } catch (SAPWebServiceException ex) {
                        failServiceCall(serviceCall, extension, (MessageSeeds)ex.getMessageSeed(), ex.getMessageArgs());
                        return;
                    }
                    serviceCall.requestTransition(DefaultState.SUCCESSFUL);
                } else {
                    failServiceCall(serviceCall, extension, MessageSeeds.REGISTER_NOT_FOUND, obis);
                }
            } else {
                List<Channel> channels = findChannelOnDevice(device.get(), obis, interval);
                if (!channels.isEmpty()) {
                    if (channels.size() == 1) {
                        try {
                            sapCustomPropertySets.setLrn(channels.get(0), extension.getLrn(), extension.getStartDate(), extension.getEndDate());
                        } catch (SAPWebServiceException ex) {
                            failServiceCall(serviceCall, extension, (MessageSeeds)ex.getMessageSeed(), ex.getMessageArgs());
                            return;
                        }
                        serviceCall.requestTransition(DefaultState.SUCCESSFUL);
                    } else {
                        failServiceCall(serviceCall, extension, MessageSeeds.SEVERAL_CHANNELS, obis);
                    }
                } else {
                    failServiceCall(serviceCall, extension, MessageSeeds.CHANNEL_NOT_FOUND, obis, interval);
                }
            }
        } else {
            failServiceCall(serviceCall, extension, MessageSeeds.NO_DEVICE_FOUND_BY_SAP_ID, extension.getDeviceId());
        }

    }

    private void cancelServiceCall(ServiceCall serviceCall) {
        UtilitiesDeviceRegisterCreateRequestDomainExtension extension = serviceCall.getExtensionFor(new UtilitiesDeviceRegisterCreateRequestCustomPropertySet()).get();
        extension.setError(MessageSeeds.REGISTER_SERVICE_CALL_WAS_CANCELLED, extension.getObis());
        serviceCall.update(extension);
    }

    private void failServiceCall(ServiceCall serviceCall, UtilitiesDeviceRegisterCreateRequestDomainExtension extension, MessageSeeds messageSeed, Object... args){
        extension.setError(messageSeed, args);
        serviceCall.update(extension);
        serviceCall.requestTransition(DefaultState.FAILED);
    }

    private List<Channel> findChannelOnDevice(Device device, String obis, String interval) {
        return device.getChannels().stream().filter(c -> c.getObisCode().toString().equals(obis))
                .filter(c -> c.getInterval().getSeconds() / 60 == Integer.parseInt(interval))
                .collect(Collectors.toList());
    }

    @Reference
    public void setSAPCustomPropertySets(SAPCustomPropertySets sapCustomPropertySets) {
        this.sapCustomPropertySets = sapCustomPropertySets;
    }
}
