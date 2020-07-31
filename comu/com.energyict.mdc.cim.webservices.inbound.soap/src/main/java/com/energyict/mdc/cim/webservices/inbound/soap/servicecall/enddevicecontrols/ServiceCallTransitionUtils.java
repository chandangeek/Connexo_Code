/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.enddevicecontrols;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;

import java.util.List;
import java.util.stream.Collectors;

public class ServiceCallTransitionUtils {

    public static void resultTransition(ServiceCall serviceCall, ServiceCallService serviceCallService) {
        List<ServiceCall> children = findAllChildren(serviceCall);
        if (areAllChildrenClosed(children)) {
            serviceCall = lock(serviceCall, serviceCallService);
            if (hasAllChildrenStates(children, DefaultState.SUCCESSFUL)) {
                transitToStateAfterOngoing(serviceCall, DefaultState.SUCCESSFUL);
            } else if (hasAllChildrenStates(children, DefaultState.CANCELLED)) {
                transitToStateAfterOngoing(serviceCall, DefaultState.CANCELLED);
            } else if (hasAnyChildState(children, DefaultState.PARTIAL_SUCCESS)
                    || hasAnyChildState(children, DefaultState.SUCCESSFUL)) {
                transitToStateAfterOngoing(serviceCall, DefaultState.PARTIAL_SUCCESS);
            } else {
                transitToStateAfterOngoing(serviceCall, DefaultState.FAILED);
            }
        }
    }

    public static boolean hasAnyChildState(List<ServiceCall> serviceCalls, DefaultState defaultState) {
        return serviceCalls.stream().anyMatch(sc -> sc.getState().equals(defaultState));
    }

    public static List<ServiceCall> findAllChildren(ServiceCall serviceCall) {
        return serviceCall.findChildren().stream().collect(Collectors.toList());
    }

    public static boolean hasAllChildrenStates(List<ServiceCall> serviceCalls, DefaultState defaultState) {
        return serviceCalls.stream().allMatch(sc -> sc.getState().equals(defaultState));
    }

    private static boolean areAllChildrenClosed(List<ServiceCall> serviceCalls) {
        return serviceCalls.stream()
                .allMatch(sc -> !sc.getState().isOpen());
    }

    public static void transitToStateAfterOngoing(ServiceCall serviceCall, DefaultState finalState) {
        if (serviceCall.getState() != finalState) {
            if (serviceCall.getState() != DefaultState.ONGOING) {
                serviceCall.requestTransition(DefaultState.ONGOING);
            }
            if (finalState != DefaultState.ONGOING) {
                serviceCall.requestTransition(finalState);
            }
        }
    }

    public static ServiceCall lock(ServiceCall serviceCall, ServiceCallService serviceCallService) {
        return serviceCallService.lockServiceCall(serviceCall.getId())
                .orElseThrow(() -> new IllegalStateException("Service call " + serviceCall.getNumber() + " disappeared."));
    }
}
