/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.task;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFilter;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.energyict.mdc.sap.soap.webservices.impl.AdditionalProperties;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallTypes;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection.ConnectionStatusChangeDomainExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class CheckStatusChangeCancellationHandler implements TaskExecutor {

    private final ServiceCallService serviceCallService;
    private final Clock clock;
    private final WebServiceActivator webServiceActivator;

    public CheckStatusChangeCancellationHandler(ServiceCallService serviceCallService, Clock clock,
                                                WebServiceActivator webServiceActivator) {
        this.serviceCallService = serviceCallService;
        this.clock = clock;
        this.webServiceActivator = webServiceActivator;
    }

    @Override
    public void execute(TaskOccurrence occurrence) {
        long timeoutMinutes = webServiceActivator.getSapProperty(AdditionalProperties.CHECK_STATUS_CHANGE_TIMEOUT);
        Instant now = clock.instant();
        Finder<ServiceCall> serviceCallFinder = findAvailableServiceCalls(ServiceCallTypes.CONNECTION_STATUS_CHANGE);
        serviceCallFinder
                .stream()
                .forEach(serviceCall -> {
                    ConnectionStatusChangeDomainExtension extension = serviceCall.getExtension(ConnectionStatusChangeDomainExtension.class).get();
                    Instant maxDate = extension.getProcessDate().isAfter(serviceCall.getCreationTime()) ? extension.getProcessDate() : serviceCall.getCreationTime();
                    if (Duration.between(maxDate, now).get(ChronoUnit.SECONDS) > timeoutMinutes * 60) {
                        serviceCall.log(LogLevel.INFO, "Cancelling service call by timeout");
                        serviceCall.transitionWithLockIfPossible(DefaultState.CANCELLED);
                    }
                });
    }

    private Finder<ServiceCall> findAvailableServiceCalls(ServiceCallTypes serviceCallType) {
        ServiceCallFilter filter = new ServiceCallFilter();
        filter.types.add(serviceCallType.getTypeName());
        filter.states.add(DefaultState.SCHEDULED.name());
        filter.states.add(DefaultState.PAUSED.name());
        filter.states.add(DefaultState.ONGOING.name());
        filter.states.add(DefaultState.WAITING.name());
        filter.states.add(DefaultState.PENDING.name());
        return serviceCallService.getServiceCallFinder(filter);
    }
}
