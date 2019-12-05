/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallHelper;

import org.osgi.service.component.annotations.Component;

import java.util.List;


@Component(name = SubMasterMeterRegisterChangeRequestServiceCallHandler.NAME, service = ServiceCallHandler.class,
        property = "name=" + SubMasterMeterRegisterChangeRequestServiceCallHandler.NAME, immediate = true)
public class SubMasterMeterRegisterChangeRequestServiceCallHandler implements ServiceCallHandler {

    public static final String NAME = "SubMasterMeterRegisterChangeRequest";
    public static final String VERSION = "v1.0";
    public static final String APPLICATION = "MDC";

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
                            .filter(child -> child.canTransitionTo(DefaultState.ONGOING))
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
    public void onChildStateChange(ServiceCall subParentServiceCall, ServiceCall childServiceCall, DefaultState oldState, DefaultState newState) {
        switch (newState) {
            case CANCELLED:
            case FAILED:
            case SUCCESSFUL:
                if (ServiceCallHelper.isLastChild(ServiceCallHelper.findChildren(subParentServiceCall))) {
                    if (subParentServiceCall.getState().equals(DefaultState.PENDING)) {
                        subParentServiceCall.requestTransition(DefaultState.ONGOING);
                    } else if (subParentServiceCall.getState().equals(DefaultState.SCHEDULED)) {
                        subParentServiceCall.requestTransition(DefaultState.PENDING);
                        subParentServiceCall.requestTransition(DefaultState.ONGOING);
                    }
                }
            default:
                // No specific action required for these states
                break;
        }
    }

    private void resultTransition(ServiceCall subParent) {
        List<ServiceCall> children = ServiceCallHelper.findChildren(subParent);
        if (ServiceCallHelper.isLastChild(children)) {
            if (subParent.getState().equals(DefaultState.PENDING)) {
                subParent.requestTransition(DefaultState.ONGOING);
            } else if (ServiceCallHelper.hasAllChildrenInState(children, DefaultState.SUCCESSFUL) && subParent.canTransitionTo(DefaultState.SUCCESSFUL)) {
                subParent.requestTransition(DefaultState.SUCCESSFUL);
            } else if ((ServiceCallHelper.hasAnyChildState(children, DefaultState.PARTIAL_SUCCESS) || ServiceCallHelper.hasAnyChildState(children, DefaultState.SUCCESSFUL)) && subParent.canTransitionTo(DefaultState.PARTIAL_SUCCESS)) {
                subParent.requestTransition(DefaultState.PARTIAL_SUCCESS);
            } else if (subParent.canTransitionTo(DefaultState.FAILED)) {
                subParent.requestTransition(DefaultState.FAILED);
            }
        }
    }
}
