/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement;

import com.elster.jupiter.metering.EndDeviceStage;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.meterreplacement.MeterRegisterBulkChangeConfirmationMessage;
import com.energyict.mdc.sap.soap.webservices.impl.meterreplacement.MeterRegisterChangeConfirmationMessage;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallHelper;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component(name = MasterMeterRegisterChangeRequestServiceCallHandler.NAME, service = ServiceCallHandler.class,
        property = "name=" + MasterMeterRegisterChangeRequestServiceCallHandler.NAME, immediate = true)
public class MasterMeterRegisterChangeRequestServiceCallHandler implements ServiceCallHandler {

    public static final String NAME = "MasterMeterRegisterChangeRequest";
    public static final String VERSION = "v1.0";
    public static final String APPLICATION = "MDC";

    private volatile Clock clock;
    private volatile WebServiceActivator webServiceActivator;
    private volatile SAPCustomPropertySets sapCustomPropertySets;

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        switch (newState) {
            case PENDING:
                serviceCall.findChildren().stream().forEach(child -> {
                    if (child.canTransitionTo(DefaultState.PENDING)) {
                        child.requestTransition(DefaultState.PENDING);
                    }
                });
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
            case CANCELLED:
            case FAILED:
            case PARTIAL_SUCCESS:
            case SUCCESSFUL:
                sendResultMessage(serviceCall);
                break;
            default:
                // No specific action required for these states
                break;
        }
    }

    @Override
    public void onChildStateChange(ServiceCall parentServiceCall, ServiceCall childServiceCall, DefaultState oldState, DefaultState newState) {
        switch (newState) {
            case CANCELLED:
            case FAILED:
            case SUCCESSFUL:
            case PARTIAL_SUCCESS:
            case REJECTED:
                if (ServiceCallHelper.isLastChild(ServiceCallHelper.findChildren(parentServiceCall))) {
                    if (parentServiceCall.getState().equals(DefaultState.PENDING)) {
                        parentServiceCall.requestTransition(DefaultState.ONGOING);
                    } else if (parentServiceCall.getState().equals(DefaultState.SCHEDULED)) {
                        parentServiceCall.requestTransition(DefaultState.PENDING);
                        parentServiceCall.requestTransition(DefaultState.ONGOING);
                    }
                }
                break;
            default:
                // No specific action required for these states
                break;
        }
    }

    private void sendResultMessage(ServiceCall serviceCall) {
        MasterMeterRegisterChangeRequestDomainExtension extension = serviceCall.getExtensionFor(new MasterMeterRegisterChangeRequestCustomPropertySet()).get();
        if (extension.isBulk()) {
            MeterRegisterBulkChangeConfirmationMessage resultMessage = MeterRegisterBulkChangeConfirmationMessage
                    .builder()
                    .from(serviceCall, webServiceActivator.getMeteringSystemId(), clock.instant())
                    .build();

            WebServiceActivator.METER_REGISTER_BULK_CHANGE_CONFIRMATIONS.forEach(sender -> sender.call(resultMessage));
        } else {
            MeterRegisterChangeConfirmationMessage resultMessage = MeterRegisterChangeConfirmationMessage
                    .builder()
                    .from(serviceCall, webServiceActivator.getMeteringSystemId(), clock.instant())
                    .build();

            WebServiceActivator.METER_REGISTER_CHANGE_CONFIRMATIONS.forEach(sender -> sender.call(resultMessage));
        }

        try {
            List<String> deviceIds = findIdsOfActiveDevicesWithLRN(ServiceCallHelper.findChildren(serviceCall));
            if (!deviceIds.isEmpty()) {
                if (extension.isBulk()) {
                    WebServiceActivator.UTILITIES_DEVICE_REGISTERED_BULK_NOTIFICATION.forEach(sender -> sender.call(deviceIds));
                } else {
                    WebServiceActivator.UTILITIES_DEVICE_REGISTERED_NOTIFICATION.forEach(sender -> sender.call(deviceIds.get(0)));
                }
            }
        } catch (Exception ex) {
            //If we could not send registered notification due to any exception, we should continue to process service call
            serviceCall.log(LogLevel.WARNING, "Exception while sending registered (bulk) notification: " + ex.getLocalizedMessage());
        }
    }

    private List<String> findIdsOfActiveDevicesWithLRN(List<ServiceCall> children) {
        List<String> deviceIds = new ArrayList<>();
        children.forEach(child -> {
            SubMasterMeterRegisterChangeRequestDomainExtension extension = child.getExtensionFor(new SubMasterMeterRegisterChangeRequestCustomPropertySet()).get();
            if (extension.isCreateRequest()) {
                String deviceId = extension.getDeviceId();
                Optional<Device> device = sapCustomPropertySets.getDevice(deviceId);
                if (device.isPresent() && device.get().getStage().getName().equals(EndDeviceStage.OPERATIONAL.getKey()) &&
                        !sapCustomPropertySets.isRegistered(device.get()) &&
                        (child.getState() == DefaultState.SUCCESSFUL || ServiceCallHelper.hasAnyChildState(ServiceCallHelper.findChildren(child), DefaultState.SUCCESSFUL))) {
                    deviceIds.add(deviceId);
                }
            }
        });

        return deviceIds;
    }


    private void resultTransition(ServiceCall parent) {
        List<ServiceCall> children = ServiceCallHelper.findChildren(parent);
        if (ServiceCallHelper.isLastChild(children)) {
            if (parent.getState().equals(DefaultState.PENDING) && parent.canTransitionTo(DefaultState.ONGOING)) {
                parent.requestTransition(DefaultState.ONGOING);
            } else if (ServiceCallHelper.hasAllChildrenInState(children, DefaultState.SUCCESSFUL) && parent.canTransitionTo(DefaultState.SUCCESSFUL)) {
                parent.requestTransition(DefaultState.SUCCESSFUL);
            } else if ((ServiceCallHelper.hasAnyChildState(children, DefaultState.PARTIAL_SUCCESS) || ServiceCallHelper.hasAnyChildState(children, DefaultState.SUCCESSFUL)) && parent.canTransitionTo(DefaultState.PARTIAL_SUCCESS)) {
                parent.requestTransition(DefaultState.PARTIAL_SUCCESS);
            } else if (parent.canTransitionTo(DefaultState.FAILED)) {
                parent.requestTransition(DefaultState.FAILED);
            }
        }
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setWebServiceActivator(WebServiceActivator webServiceActivator) {
        this.webServiceActivator = webServiceActivator;
    }

    @Reference
    public void setSAPCustomPropertySets(SAPCustomPropertySets sapCustomPropertySets) {
        this.sapCustomPropertySets = sapCustomPropertySets;
    }
}
