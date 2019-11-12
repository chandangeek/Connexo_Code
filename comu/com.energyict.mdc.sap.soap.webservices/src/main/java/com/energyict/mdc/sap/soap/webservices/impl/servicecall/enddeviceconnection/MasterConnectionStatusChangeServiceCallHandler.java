/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.servicecall.ServiceCallService;

import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallHelper;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

@Component(name = MasterConnectionStatusChangeServiceCallHandler.NAME, service = ServiceCallHandler.class,
        property = "name=" + MasterConnectionStatusChangeServiceCallHandler.NAME, immediate = true)
public class MasterConnectionStatusChangeServiceCallHandler implements ServiceCallHandler {

    private volatile ServiceCallService serviceCallService;

    public static final String NAME = "MasterConnectionStatusChangeServiceCallHandler";
    public static final String VERSION = "v1.0";
    public static final String APPLICATION = "MDC";

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        switch (newState) {
            case CANCELLED:
            case FAILED:
            case PARTIAL_SUCCESS:
            case SUCCESSFUL:
                serviceCall.log(LogLevel.INFO, "All sub-parent service call operations have been executed");
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
            case PARTIAL_SUCCESS:
            case SUCCESSFUL:
                resultTransition(parentServiceCall);
            default:
                // No specific action required for these states
                break;
        }
    }

    private void resultTransition(ServiceCall parent) {
        List<ServiceCall> children = ServiceCallHelper.findChildren(parent);
        if (ServiceCallHelper.isLastChild(children)) {
            if (ServiceCallHelper.hasAllChildrenInState(children, DefaultState.SUCCESSFUL)) {
                transitWithLock(parent, DefaultState.SUCCESSFUL);
            } else if (ServiceCallHelper.hasAllChildrenInState(children, DefaultState.CANCELLED)) {
                transitWithLock(parent, DefaultState.CANCELLED);
            } else if (ServiceCallHelper.hasAnyChildState(children, DefaultState.SUCCESSFUL)) {
                transitWithLock(parent, DefaultState.PARTIAL_SUCCESS);
            } else if (parent.canTransitionTo(DefaultState.FAILED)) {
                transitWithLock(parent, DefaultState.FAILED);
            }
        }
    }

    private void transitWithLock(ServiceCall serviceCall, DefaultState finalState) {
        ServiceCall lockedServiceCall = serviceCallService.lockServiceCall(serviceCall.getId()).orElseThrow(() -> new IllegalStateException("Unable to lock service call"));
        if (lockedServiceCall.getState().equals(DefaultState.WAITING) && lockedServiceCall.canTransitionTo(DefaultState.ONGOING)) {
            lockedServiceCall.requestTransition(DefaultState.ONGOING);
        }
        if (lockedServiceCall.canTransitionTo(finalState)) {
            lockedServiceCall.requestTransition(finalState);
        }
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

}
