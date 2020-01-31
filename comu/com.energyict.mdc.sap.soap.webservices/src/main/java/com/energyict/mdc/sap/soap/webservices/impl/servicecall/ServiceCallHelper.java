/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.servicecall;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;

import java.util.List;
import java.util.stream.Collectors;

public class ServiceCallHelper {
    public static boolean isLastChild(List<ServiceCall> serviceCalls) {
        return serviceCalls.stream().noneMatch(sc -> sc.getState().isOpen());
    }

    public static boolean isLastPausedChild(List<ServiceCall> serviceCalls) {
        return serviceCalls.stream().allMatch(sc -> !sc.getState().isOpen() || sc.getState().equals(DefaultState.PAUSED));
    }

    public static boolean hasAllChildrenInState(List<ServiceCall> serviceCalls, DefaultState defaultState) {
        return serviceCalls.stream().allMatch(sc -> sc.getState().equals(defaultState));
    }

    public static List<ServiceCall> findChildren(ServiceCall serviceCall) {
        return serviceCall.findChildren().stream().collect(Collectors.toList());
    }

    public static boolean hasAnyChildState(List<ServiceCall> serviceCalls, DefaultState defaultState) {
        return serviceCalls.stream().anyMatch(sc -> sc.getState().equals(defaultState));
    }
}