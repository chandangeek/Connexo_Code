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
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallTypes;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MasterMeterReadingDocumentCreateResultDomainExtension;

import java.time.Clock;

public class CheckConfirmationTimeoutHandler implements TaskExecutor {

    private final Clock clock;
    private final ServiceCallService serviceCallService;

    public CheckConfirmationTimeoutHandler(Clock clock, ServiceCallService serviceCallService, SAPCustomPropertySets sapCustomPropertySets) {
        this.clock = clock;
        this.serviceCallService = serviceCallService;
    }

    @Override
    public void execute(TaskOccurrence occurrence) {
        findAvailableServiceCalls(ServiceCallTypes.MASTER_METER_READING_DOCUMENT_CREATE_RESULT)
                .stream()
                .forEach(serviceCall -> {
                    MasterMeterReadingDocumentCreateResultDomainExtension extension = serviceCall.getExtension(MasterMeterReadingDocumentCreateResultDomainExtension.class)
                            .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));
                    if (extension.getConfirmationTime() != null && extension.getConfirmationTime().isBefore(clock.instant())) {
                        serviceCall.requestTransition(DefaultState.ONGOING);
                        serviceCall.requestTransition(DefaultState.FAILED);
                    }
                });
    }

    private Finder<ServiceCall> findAvailableServiceCalls(ServiceCallTypes serviceCallType) {
        ServiceCallFilter filter = new ServiceCallFilter();
        filter.types.add(serviceCallType.getTypeName());
        filter.states.add(DefaultState.PENDING.name());
        filter.states.add(DefaultState.WAITING.name());
        return serviceCallService.getServiceCallFinder(filter);
    }
}
