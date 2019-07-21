/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;

import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component(name = SubMasterUtilitiesDeviceRegisterCreateRequestCallHandler.NAME, service = ServiceCallHandler.class,
        property = "name=" + SubMasterUtilitiesDeviceRegisterCreateRequestCallHandler.NAME, immediate = true)
public class SubMasterUtilitiesDeviceRegisterCreateRequestCallHandler implements ServiceCallHandler {

    public static final String NAME = "SubMasterUtilitiesDeviceRegisterCreateRequestCallHandler";
    public static final String VERSION = "v1.0";

    private volatile SAPCustomPropertySets sapCustomPropertySets;


    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        switch (newState) {
            case PENDING:
                serviceCall.findChildren().stream().forEach(child -> {
                    if(child.canTransitionTo(DefaultState.PENDING)) {
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

    private void resultTransition(ServiceCall parent) {
        List<ServiceCall> children = findChildren(parent);
        if (isLastChild(children)) {
            if (parent.getState().equals(DefaultState.PENDING) && parent.canTransitionTo(DefaultState.ONGOING)) {
                parent.requestTransition(DefaultState.ONGOING);
            } else if (hasAllChildState(children, DefaultState.SUCCESSFUL) && parent.canTransitionTo(DefaultState.SUCCESSFUL)) {
                parent.requestTransition(DefaultState.SUCCESSFUL);
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

    @Reference
    public void setSAPCustomPropertySets(SAPCustomPropertySets sapCustomPropertySets) {
        this.sapCustomPropertySets = sapCustomPropertySets;
    }
}
