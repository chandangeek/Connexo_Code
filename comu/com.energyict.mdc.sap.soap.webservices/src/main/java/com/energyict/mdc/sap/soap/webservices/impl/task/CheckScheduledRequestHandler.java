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
import com.energyict.mdc.sap.soap.webservices.impl.AdditionalProperties;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallTypes;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MeterReadingDocumentCreateResultDomainExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;

public class CheckScheduledRequestHandler implements TaskExecutor {

    private final ServiceCallService serviceCallService;
    private final Clock clock;

    public CheckScheduledRequestHandler(ServiceCallService serviceCallService, Clock clock) {
        this.serviceCallService = serviceCallService;
        this.clock = clock;
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
                .forEach(serviceCall -> {
                            MeterReadingDocumentCreateResultDomainExtension domainExtension = serviceCall.getExtension(MeterReadingDocumentCreateResultDomainExtension.class).get();
                            //if next reading attempt date is null, so reading has not started yet
                            Instant nextReadingAttemptDate = domainExtension.getNextReadingAttemptDate();
                            if (nextReadingAttemptDate == null) {
                                serviceCall.requestTransition(DefaultState.ONGOING);
                            } else {
                                if (clock.instant().isAfter(nextReadingAttemptDate)) {
                                    serviceCall.requestTransition(DefaultState.ONGOING);
                                }
                            }
                        }
                );
    }

    private Finder<ServiceCall> findAvailableServiceCalls(ServiceCallTypes serviceCallType) {
        ServiceCallFilter filter = new ServiceCallFilter();
        filter.types.add(serviceCallType.getTypeName());
        filter.states.add(DefaultState.PAUSED.name());
        filter.states.add(DefaultState.SCHEDULED.name());
        return serviceCallService.getServiceCallFinder(filter);
    }
}
