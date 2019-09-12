/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.task;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallTypes;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MasterMeterReadingDocumentCreateResultDomainExtension;

import java.time.Clock;
import java.util.EnumSet;

public class CheckConfirmationTimeoutHandler implements TaskExecutor {

    private final Clock clock;
    private final ServiceCallService serviceCallService;

    public CheckConfirmationTimeoutHandler(Clock clock, ServiceCallService serviceCallService) {
        this.clock = clock;
        this.serviceCallService = serviceCallService;
    }

    @Override
    public void execute(TaskOccurrence occurrence) {
        serviceCallService.findAvailableServiceCalls(ServiceCallTypes.MASTER_METER_READING_DOCUMENT_CREATE_RESULT.getTypeName(), EnumSet.of(DefaultState.PENDING, DefaultState.WAITING))
                .stream()
                .forEach(serviceCall -> {
                    MasterMeterReadingDocumentCreateResultDomainExtension extension = serviceCall.getExtension(MasterMeterReadingDocumentCreateResultDomainExtension.class)
                            .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));
                    if (extension.getConfirmationTime() != null && extension.getConfirmationTime().isBefore(clock.instant())) {
                        serviceCall.findChildren().stream().forEach(child -> {
                            serviceCallService.transitionWithLockIfPossible(child, DefaultState.ONGOING, DefaultState.FAILED);
                        });
                    }
                });
    }
}
