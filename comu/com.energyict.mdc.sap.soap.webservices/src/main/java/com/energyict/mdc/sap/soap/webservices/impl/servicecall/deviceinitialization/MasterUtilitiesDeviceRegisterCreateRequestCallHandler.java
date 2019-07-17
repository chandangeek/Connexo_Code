/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization;

import com.elster.jupiter.metering.EndDeviceStage;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.registercreation.UtilitiesDeviceRegisterCreateConfirmationMessage;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component(name = MasterUtilitiesDeviceRegisterCreateRequestCallHandler.NAME, service = ServiceCallHandler.class,
        property = "name=" + MasterUtilitiesDeviceRegisterCreateRequestCallHandler.NAME, immediate = true)
public class MasterUtilitiesDeviceRegisterCreateRequestCallHandler implements ServiceCallHandler {

    public static final String NAME = "MasterUtilitiesDeviceRegisterCreateRequestCallHandler";
    public static final String VERSION = "v1.0";

    private volatile Clock clock;
    private volatile SAPCustomPropertySets sapCustomPropertySets;


    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        switch (newState) {
            case PENDING:
                serviceCall.findChildren().stream().forEach(child -> child.requestTransition(DefaultState.PENDING));
                break;
            case ONGOING:
                if (oldState.equals(DefaultState.PAUSED)) {
                    serviceCall.findChildren()
                            .stream()
                            .filter(child -> child.getState().isOpen())
                            .forEach(child -> child.requestTransition(DefaultState.ONGOING));
                } else {
                    resultTransition(serviceCall);
                }
                break;
            case FAILED:
            case PARTIAL_SUCCESS:
            case SUCCESSFUL:
                sendResultMessage(serviceCall);
            default:
                // No specific action required for these states
                break;
        }
    }

    @Override
    public void onChildStateChange(ServiceCall parentServiceCall, ServiceCall childServiceCall, DefaultState oldState, DefaultState newState) {
        switch (newState) {
            case FAILED:
            case SUCCESSFUL:
                if (isLastChild(findChildren(parentServiceCall))) {
                    if (parentServiceCall.getState().equals(DefaultState.PENDING)) {
                        parentServiceCall.requestTransition(DefaultState.ONGOING);
                    } else if (parentServiceCall.getState().equals(DefaultState.SCHEDULED)) {
                        parentServiceCall.requestTransition(DefaultState.PENDING);
                        parentServiceCall.requestTransition(DefaultState.ONGOING);
                    }
                }
            default:
                // No specific action required for these states
                break;
        }
    }

    private void sendResultMessage(ServiceCall serviceCall) {
        MasterUtilitiesDeviceRegisterCreateRequestDomainExtension extension = serviceCall.getExtensionFor(new MasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet()).get();
        List<ServiceCall> children = findChildren(serviceCall);
        UtilitiesDeviceRegisterCreateConfirmationMessage resultMessage = UtilitiesDeviceRegisterCreateConfirmationMessage
                .builder()
                .from(serviceCall, children, clock.instant(), extension.isBulk())
                .build();
        if (extension.isBulk()) {
            WebServiceActivator.UTILITIES_DEVICE_REGISTER_BULK_CREATE_CONFIRMATION.forEach(sender -> sender.call(resultMessage));
        } else {
            WebServiceActivator.UTILITIES_DEVICE_REGISTER_CREATE_CONFIRMATION.forEach(sender -> sender.call(resultMessage));
        }


        //check device active and send registered notification
        try {
            List<String> deviceIds = createActiveAndAnyLrnListDeviceIds(children);
            if (!deviceIds.isEmpty()) {
                if (extension.isBulk()) {
                    WebServiceActivator.UTILITIES_DEVICE_REGISTERED_BULK_NOTIFICATION.forEach(sender -> sender.call(deviceIds));
                } else {
                    WebServiceActivator.UTILITIES_DEVICE_REGISTERED_NOTIFICATION.forEach(sender -> sender.call(deviceIds.get(0)));
                }
            }
        }catch(Exception ex){}
    }

    private List<String> createActiveAndAnyLrnListDeviceIds(List<ServiceCall> children) {
        List<String> deviceIds = new ArrayList<>();
        children.forEach(child -> {
            SubMasterUtilitiesDeviceRegisterCreateRequestDomainExtension extension = child.getExtensionFor(new SubMasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet()).get();
            String deviceId = extension.getDeviceId();
            Optional<Device> device = sapCustomPropertySets.getDevice(deviceId);
            if (device.isPresent() && device.get().getStage().getName().equals(EndDeviceStage.OPERATIONAL.getKey()) &&
                    (child.getState() == DefaultState.SUCCESSFUL || hasAnyChildState(findChildren(child), DefaultState.SUCCESSFUL))) {
                deviceIds.add(deviceId);
            }
        });

        return deviceIds;
    }

    private void resultTransition(ServiceCall parent) {
        List<ServiceCall> children = findChildren(parent);
        if (isLastChild(children)) {
            if (parent.getState().equals(DefaultState.PENDING) && parent.canTransitionTo(DefaultState.ONGOING)) {
                parent.requestTransition(DefaultState.ONGOING);
            } else if (hasAllChildState(children, DefaultState.SUCCESSFUL) && parent.canTransitionTo(DefaultState.SUCCESSFUL)) {
                parent.requestTransition(DefaultState.SUCCESSFUL);
            } else if (hasAnyChildState(children, DefaultState.SUCCESSFUL) && parent.canTransitionTo(DefaultState.PARTIAL_SUCCESS)) {
                parent.requestTransition(DefaultState.PARTIAL_SUCCESS);
            } else if (parent.canTransitionTo(DefaultState.FAILED)) {
                parent.requestTransition(DefaultState.FAILED);
            }
        }
    }

    private boolean isLastChild(List<ServiceCall> serviceCalls) {
        return serviceCalls.stream().noneMatch(sc -> sc.getState().isOpen());
    }

    private boolean hasAllChildState(List<ServiceCall> serviceCalls, DefaultState defaultState) {
        return serviceCalls.stream().allMatch(sc -> sc.getState().equals(defaultState));
    }

    private List<ServiceCall> findChildren(ServiceCall serviceCall) {
        return serviceCall.findChildren().stream().collect(Collectors.toList());
    }

    private boolean hasAnyChildState(List<ServiceCall> serviceCalls, DefaultState defaultState) {
        return serviceCalls.stream().anyMatch(sc -> sc.getState().equals(defaultState));
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setSAPCustomPropertySets(SAPCustomPropertySets sapCustomPropertySets) {
        this.sapCustomPropertySets = sapCustomPropertySets;
    }
}
