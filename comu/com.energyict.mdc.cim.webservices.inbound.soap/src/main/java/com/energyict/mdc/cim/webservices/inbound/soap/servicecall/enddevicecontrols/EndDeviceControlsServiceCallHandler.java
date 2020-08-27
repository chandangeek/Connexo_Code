/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.enddevicecontrols;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.util.Checks;

import com.energyict.mdc.cim.webservices.inbound.soap.impl.DeviceCommandInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.HeadEndController;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import ch.iec.tc57._2011.enddevicecontrols.EndDeviceControlAttribute;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EndDeviceControlsServiceCallHandler implements ServiceCallHandler {
    public static final String SERVICE_CALL_HANDLER_NAME = "EndDeviceControls";
    public static final String VERSION = "v1.0";
    public static final String APPLICATION = "MDC";

    private final ServiceCallService serviceCallService;
    private final HeadEndController headEndController;
    private final Thesaurus thesaurus;
    private final DeviceService deviceService;

    @Inject
    public EndDeviceControlsServiceCallHandler(ServiceCallService serviceCallService, HeadEndController headEndController,
                                               Thesaurus thesaurus, DeviceService deviceService) {
        this.serviceCallService = serviceCallService;
        this.headEndController = headEndController;
        this.thesaurus = thesaurus;
        this.deviceService = deviceService;
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        switch (newState) {
            case PENDING:
                serviceCall.transitionWithLockIfPossible(DefaultState.ONGOING);
                break;
            case ONGOING:
                if (oldState.equals(DefaultState.PENDING)) {
                    createHeadEndServiceCall(serviceCall);
                }
                break;
            case CANCELLED:
                cancelServiceCall(serviceCall);
                break;
            default:
                // No specific action required for these states
                break;
        }
    }

    @Override
    public void onChildStateChange(ServiceCall parentServiceCall, ServiceCall subParentServiceCall, DefaultState oldState, DefaultState newState) {
        if (!newState.isOpen()) {
            ServiceCallTransitionUtils.resultTransition(parentServiceCall, serviceCallService);
        }
    }

    private void createHeadEndServiceCall(ServiceCall serviceCall) {
        serviceCall = ServiceCallTransitionUtils.lock(serviceCall, serviceCallService);
        if (serviceCall.getState().equals(DefaultState.ONGOING)) {
            EndDeviceControlsDomainExtension extension = serviceCall.getExtension(EndDeviceControlsDomainExtension.class)
                    .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));

            Optional<Device> optionalDevice;
            if (extension.getDeviceMrid() != null) {
                optionalDevice = deviceService.findDeviceByMrid(extension.getDeviceMrid());
                if (!optionalDevice.isPresent()) {
                    failServiceCall(serviceCall, extension, MessageSeeds.NO_DEVICE_WITH_MRID, extension.getDeviceMrid());
                    return;
                }
            } else {
                optionalDevice = deviceService.findDeviceByName(extension.getDeviceName());
                if (!optionalDevice.isPresent()) {
                    failServiceCall(serviceCall, extension, MessageSeeds.NO_DEVICE_WITH_NAME, extension.getDeviceName());
                    return;
                }
            }

            EndDevice endDevice = optionalDevice.get().getMeter();
            serviceCall.setTargetObject(optionalDevice.get());
            serviceCall.save();
            try {
                ServiceCall parentServiceCall = serviceCall.getParent()
                        .orElseThrow(() -> new IllegalStateException("Unable to get parent service call for service call"));
                SubMasterEndDeviceControlsDomainExtension parentExtension = parentServiceCall.getExtension(SubMasterEndDeviceControlsDomainExtension.class)
                        .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));

                List<EndDeviceControlAttribute> attributes = new ArrayList<>();
                if (!Checks.is(parentExtension.getCommandAttributes()).emptyOrOnlyWhiteSpace()) {
                    attributes = Arrays.stream(parentExtension.getCommandAttributes().split(";"))
                            .map(e -> e.split("="))
                            .map(e -> {
                                EndDeviceControlAttribute attr = new EndDeviceControlAttribute();
                                attr.setName(e[0]);
                                attr.setValue(e[1]);
                                return attr;
                            })
                            .collect(Collectors.toList());
                }

                DeviceCommandInfo deviceCommandInfo = headEndController.checkOperation(parentExtension.getCommandCode(), attributes);

                headEndController.performOperations(endDevice, serviceCall, deviceCommandInfo, extension.getTriggerDate());
            } catch (Exception ex) {
                failServiceCall(serviceCall, extension, ex.getLocalizedMessage());
                return;
            }

            serviceCall.requestTransition(DefaultState.WAITING);
        }
    }

    private void failServiceCall(ServiceCall serviceCall, EndDeviceControlsDomainExtension extension, MessageSeeds messageSeed, Object... args) {
        failServiceCall(serviceCall, extension, messageSeed.translate(thesaurus, args));
    }

    private void failServiceCall(ServiceCall serviceCall, EndDeviceControlsDomainExtension extension, String error) {
        extension.setError(error);
        serviceCall.update(extension);

        serviceCall.requestTransition(DefaultState.FAILED);
    }

    private void cancelServiceCall(ServiceCall serviceCall) {
        EndDeviceControlsDomainExtension extension = serviceCall
                .getExtension(EndDeviceControlsDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));
        if (extension.getCancellationReason() == CancellationReason.NOT_CANCELLED) {
            extension.setCancellationReason(CancellationReason.MANUALLY);
            serviceCall.update(extension);
        }
    }
}
