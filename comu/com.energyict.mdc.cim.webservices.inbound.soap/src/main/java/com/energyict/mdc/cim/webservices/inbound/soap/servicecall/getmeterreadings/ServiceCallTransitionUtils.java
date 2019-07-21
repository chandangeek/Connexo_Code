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

    public static void resultTransition(ServiceCall serviceCall, boolean trickyLogic) {
        List<ServiceCall> childs = findAllChildren(serviceCall);
        if (isLastChild(childs) && (serviceCall.getState() == DefaultState.WAITING
                || serviceCall.getState() == DefaultState.ONGOING)) {
            if (hasAllChildrenStates(childs, DefaultState.SUCCESSFUL)) {
                if (trickyLogic) {
                    transitServiceCallToResultState(serviceCall, DefaultState.PAUSED);
                    transitServiceCallToResultState(serviceCall, DefaultState.ONGOING);
                } else {
                    transitServiceCallToResultState(serviceCall, DefaultState.SUCCESSFUL);
                }
            } else if (hasAllChildrenStates(childs, DefaultState.CANCELLED)) {
                transitServiceCallToResultState(serviceCall, DefaultState.CANCELLED);
            } else if (hasAllChildrenStates(childs, DefaultState.PARTIAL_SUCCESS)
                    || hasAnyChildState(childs, DefaultState.SUCCESSFUL)) {
                if (trickyLogic) {
                    transitServiceCallToResultState(serviceCall, DefaultState.PAUSED);
                    transitServiceCallToResultState(serviceCall, DefaultState.ONGOING);
                } else {
                    transitServiceCallToResultState(serviceCall, DefaultState.PARTIAL_SUCCESS);
                }
            } else {
                transitServiceCallToResultState(serviceCall, DefaultState.FAILED);
            }
        }
    }

    public static boolean isFinalState(ServiceCall serviceCall) {
        return serviceCall.getState().equals(DefaultState.CANCELLED)
                || serviceCall.getState().equals(DefaultState.FAILED)
                || serviceCall.getState().equals(DefaultState.REJECTED)
                || serviceCall.getState().equals(DefaultState.SUCCESSFUL)
                || serviceCall.getState().equals(DefaultState.PARTIAL_SUCCESS);
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
                .allMatch(sc -> isFinalState(sc));
    }

    private static void transitServiceCallToResultState(ServiceCall serviceCall, DefaultState finalState) {
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
