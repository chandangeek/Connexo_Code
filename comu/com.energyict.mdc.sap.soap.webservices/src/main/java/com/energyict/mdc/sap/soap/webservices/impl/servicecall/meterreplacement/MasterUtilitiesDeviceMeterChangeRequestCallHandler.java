/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.meterreplacement.meterchange.UtilitiesDeviceMeterChangeConfirmationMessage;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Clock;
import java.util.List;
import java.util.stream.Collectors;

@Component(name = MasterUtilitiesDeviceMeterChangeRequestCallHandler.NAME, service = ServiceCallHandler.class,
        property = "name=" + MasterUtilitiesDeviceMeterChangeRequestCallHandler.NAME, immediate = true)
public class MasterUtilitiesDeviceMeterChangeRequestCallHandler implements ServiceCallHandler {

    public static final String NAME = "MasterUtilitiesDeviceMeterChangeRequestCallHandler";
    public static final String VERSION = "v1.0";
    public static final String APPLICATION = "MDC";

    private volatile Clock clock;
    private volatile WebServiceActivator webServiceActivator;

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
                resultTransition(serviceCall);
                break;
            case FAILED:
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
            case FAILED:
            case SUCCESSFUL:
                if (isLastChild(findChildren(parentServiceCall))) {
                    if (parentServiceCall.getState().equals(DefaultState.PENDING)) {
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
        MasterUtilitiesDeviceMeterChangeRequestDomainExtension extension = serviceCall.getExtensionFor(new MasterUtilitiesDeviceMeterChangeRequestCustomPropertySet()).get();
        UtilitiesDeviceMeterChangeConfirmationMessage resultMessage = UtilitiesDeviceMeterChangeConfirmationMessage
                .builder()
                .from(serviceCall, findChildren(serviceCall), webServiceActivator.getMeteringSystemId(), clock.instant())
                .build();
        WebServiceActivator.UTILITIES_DEVICE_METER_CHANGE_CONFIRMATION_PORTS.forEach(sender -> sender.call(resultMessage));
    }

    private void resultTransition(ServiceCall parent) {
        List<ServiceCall> children = findChildren(parent);
        if (isLastChild(children)) {
            if (parent.getState().equals(DefaultState.PENDING) && parent.canTransitionTo(DefaultState.ONGOING)) {
                parent.requestTransition(DefaultState.ONGOING);
            } else if (hasAllChildrenInState(children, DefaultState.SUCCESSFUL) && parent.canTransitionTo(DefaultState.SUCCESSFUL)) {
                parent.requestTransition(DefaultState.SUCCESSFUL);
            } else if (parent.canTransitionTo(DefaultState.FAILED)) {
                parent.requestTransition(DefaultState.FAILED);
            }
        }
    }

    private boolean isLastChild(List<ServiceCall> serviceCalls) {
        return serviceCalls.stream().noneMatch(sc -> sc.getState().isOpen());
    }

    private boolean hasAllChildrenInState(List<ServiceCall> serviceCalls, DefaultState defaultState) {
        return serviceCalls.stream().allMatch(sc -> sc.getState().equals(defaultState));
    }

    private List<ServiceCall> findChildren(ServiceCall serviceCall) {
        return serviceCall.findChildren().stream().collect(Collectors.toList());
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setWebServiceActivator(WebServiceActivator webServiceActivator) {
        this.webServiceActivator = webServiceActivator;
    }
}
