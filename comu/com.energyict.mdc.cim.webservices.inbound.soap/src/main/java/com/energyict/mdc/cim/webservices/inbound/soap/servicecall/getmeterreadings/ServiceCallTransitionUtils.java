/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;

import java.util.List;
import java.util.stream.Collectors;

public class ServiceCallTransitionUtils {

    public static void resultTransition(ServiceCall serviceCall) {
        resultTransition(serviceCall, false);
    }

    public static void resultTransition(ServiceCall serviceCall, boolean initiateReading) {
        List<ServiceCall> children = findAllChildren(serviceCall);
        if (isLastChild(children) && (serviceCall.getState() == DefaultState.WAITING
                || serviceCall.getState() == DefaultState.ONGOING)) {
            if (hasAllChildrenStates(children, DefaultState.SUCCESSFUL)) {
                if (initiateReading) {
                    transitToStateAfterOngoing(serviceCall, DefaultState.PAUSED);
                    transitToStateAfterOngoing(serviceCall, DefaultState.ONGOING);
                } else {
                    transitToStateAfterOngoing(serviceCall, DefaultState.SUCCESSFUL);
                }
            } else if (hasAllChildrenStates(children, DefaultState.CANCELLED)) {
                transitToStateAfterOngoing(serviceCall, DefaultState.CANCELLED);
            } else if (hasAnyChildState(children, DefaultState.PARTIAL_SUCCESS)
                    || hasAnyChildState(children, DefaultState.SUCCESSFUL)) {
                if (initiateReading) {
                    transitToStateAfterOngoing(serviceCall, DefaultState.PAUSED);
                    transitToStateAfterOngoing(serviceCall, DefaultState.ONGOING);
                } else {
                    transitToStateAfterOngoing(serviceCall, DefaultState.PARTIAL_SUCCESS);
                }
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

    private static boolean hasAllChildrenStates(List<ServiceCall> serviceCalls, DefaultState defaultState) {
        return serviceCalls.stream().allMatch(sc -> sc.getState().equals(defaultState));
    }

    private static boolean isLastChild(List<ServiceCall> serviceCalls) {
        return serviceCalls.stream()
                .allMatch(sc -> !sc.getState().isOpen());
    }

    private static void transitToStateAfterOngoing(ServiceCall serviceCall, DefaultState finalState) {
        if (serviceCall.getState() != finalState) {
            if (serviceCall.getState() != DefaultState.ONGOING) {
                serviceCall.requestTransition(DefaultState.ONGOING);
            }
            if (finalState != DefaultState.ONGOING) {
                serviceCall.requestTransition(finalState);
            }
        }
    }
}
