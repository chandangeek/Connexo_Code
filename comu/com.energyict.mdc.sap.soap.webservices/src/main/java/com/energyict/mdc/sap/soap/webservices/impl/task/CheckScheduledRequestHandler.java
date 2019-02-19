/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.task;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFilter;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallTypes;

public class CheckScheduledRequestHandler implements TaskExecutor {

    private final ServiceCallService serviceCallService;

    public CheckScheduledRequestHandler(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Override
    public void execute(TaskOccurrence occurrence) {
        Finder<ServiceCall> serviceCallFinder = findAvailableServiceCalls(ServiceCallTypes.METER_READING_DOCUMENT_CREATE_RESULT);
        serviceCallFinder
                .stream()
                .filter(serviceCall -> serviceCall.getState().equals(DefaultState.SCHEDULED))
                .forEach(serviceCall -> serviceCall.requestTransition(DefaultState.PENDING));
        serviceCallFinder
                .stream()
                .filter(serviceCall -> serviceCall.getState().equals(DefaultState.PAUSED))
                .forEach(serviceCall -> serviceCall.requestTransition(DefaultState.ONGOING));
    }

    private Finder<ServiceCall> findAvailableServiceCalls(ServiceCallTypes serviceCallType) {
        ServiceCallFilter filter = new ServiceCallFilter();
        filter.types.add(serviceCallType.getTypeName());
        filter.states.add(DefaultState.PAUSED.name());
        filter.states.add(DefaultState.SCHEDULED.name());
        return serviceCallService.getServiceCallFinder(filter);
    }
}
