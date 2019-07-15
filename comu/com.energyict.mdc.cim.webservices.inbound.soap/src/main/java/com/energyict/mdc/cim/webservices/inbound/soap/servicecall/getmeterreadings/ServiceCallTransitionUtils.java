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
        if (isLastChild(childs)) {
            if (hasAllChildrenStates(childs, DefaultState.SUCCESSFUL)) {
                if (trickyLogic) {
                    transitServiceCallToResultState(serviceCall, DefaultState.PAUSED);
                    transitServiceCallToResultState(serviceCall, DefaultState.ONGOING);
                } else {
                    transitServiceCallToResultState(serviceCall, DefaultState.SUCCESSFUL);
                }
            } else if (hasAllChildrenStates(childs, DefaultState.CANCELLED)) {
                transitServiceCallToResultState(serviceCall, DefaultState.CANCELLED);
            } else if (hasAnyChildState(childs, DefaultState.SUCCESSFUL)) {
                transitServiceCallToResultState(serviceCall, DefaultState.PARTIAL_SUCCESS);
            } else {
                transitServiceCallToResultState(serviceCall, DefaultState.FAILED);
            }
        }
    }

    private static List<ServiceCall> findAllChildren(ServiceCall serviceCall) {
        return serviceCall.findChildren().stream().collect(Collectors.toList());
    }

    private static boolean hasAllChildrenStates(List<ServiceCall> serviceCalls, DefaultState defaultState) {
        return serviceCalls.stream().allMatch(sc -> sc.getState().equals(defaultState));
    }

    private static boolean hasAnyChildState(List<ServiceCall> serviceCalls, DefaultState defaultState) {
        return serviceCalls.stream().anyMatch(sc -> sc.getState().equals(defaultState));
    }

    private static boolean isLastChild(List<ServiceCall> serviceCalls) {
        return serviceCalls.stream()
                .allMatch(sc -> sc.getState().equals(DefaultState.CANCELLED)
                        || sc.getState().equals(DefaultState.FAILED)
                        || sc.getState().equals(DefaultState.REJECTED)
                        || sc.getState().equals(DefaultState.SUCCESSFUL));
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
