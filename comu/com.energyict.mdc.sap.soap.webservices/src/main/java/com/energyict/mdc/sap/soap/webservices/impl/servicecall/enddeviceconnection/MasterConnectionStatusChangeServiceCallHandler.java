/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;

import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallHelper;

import org.osgi.service.component.annotations.Component;

import java.util.List;

@Component(name = MasterConnectionStatusChangeServiceCallHandler.NAME, service = ServiceCallHandler.class,
        property = "name=" + MasterConnectionStatusChangeServiceCallHandler.NAME, immediate = true)
public class MasterConnectionStatusChangeServiceCallHandler implements ServiceCallHandler {

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
            if (parent.getState().equals(DefaultState.PENDING) && parent.canTransitionTo(DefaultState.ONGOING)) {
                parent.transitionWithLockIfPossible(DefaultState.ONGOING);
            } else if (ServiceCallHelper.hasAllChildrenInState(children, DefaultState.SUCCESSFUL) && parent.canTransitionTo(DefaultState.SUCCESSFUL)) {
                parent.transitionWithLockIfPossible(DefaultState.SUCCESSFUL);
            } else if (ServiceCallHelper.hasAllChildrenInState(children, DefaultState.CANCELLED)) {
                parent.transitionWithLockIfPossible(DefaultState.CANCELLED);
            } else if (ServiceCallHelper.hasAnyChildState(children, DefaultState.SUCCESSFUL) && parent.canTransitionTo(DefaultState.PARTIAL_SUCCESS)) {
                parent.transitionWithLockIfPossible(DefaultState.PARTIAL_SUCCESS);
            } else if (parent.canTransitionTo(DefaultState.FAILED)) {
                parent.transitionWithLockIfPossible(DefaultState.FAILED);
            }
        }
    }

}
