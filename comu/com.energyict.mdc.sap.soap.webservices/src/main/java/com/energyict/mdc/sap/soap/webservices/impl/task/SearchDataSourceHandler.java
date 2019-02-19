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
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MasterMeterReadingDocumentCreateRequestDomainExtension;

import java.math.BigDecimal;

public class SearchDataSourceHandler implements TaskExecutor {

    private final ServiceCallService serviceCallService;

    public SearchDataSourceHandler(ServiceCallService serviceCallService, WebServiceActivator webServiceActivator) {
        this.serviceCallService = serviceCallService;
    }

    @Override
    public void execute(TaskOccurrence taskOccurrence) {
        BigDecimal retries = new BigDecimal(WebServiceActivator.SAP_PROPERTIES.get(AdditionalProperties.REGISTER_SEARCH_ATTEMPTS));
        findAvailableServiceCalls(ServiceCallTypes.MASTER_METER_READING_DOCUMENT_CREATE_REQUEST)
                .stream()
                .forEach(serviceCall -> {
                    MasterMeterReadingDocumentCreateRequestDomainExtension domainExtension = serviceCall.getExtension(MasterMeterReadingDocumentCreateRequestDomainExtension.class).get();
                    BigDecimal retried = domainExtension.getAttemptNumber();
                    if (retried.compareTo(retries) == -1) {
                        domainExtension.setAttemptNumber(retried.add(BigDecimal.ONE));
                        serviceCall.update(domainExtension);
                        switch (serviceCall.getState()) {
                            case SCHEDULED:
                                serviceCall.requestTransition(DefaultState.PENDING);
                                break;
                            case PAUSED:
                                serviceCall.requestTransition(DefaultState.ONGOING);
                                break;
                        }
                    } else {
                        serviceCall.requestTransition(DefaultState.CANCELLED);
                    }
                });
    }

    private Finder<ServiceCall> findAvailableServiceCalls(ServiceCallTypes serviceCallType) {
        ServiceCallFilter filter = new ServiceCallFilter();
        filter.types.add(serviceCallType.getTypeName());
        filter.states.add(DefaultState.SCHEDULED.name());
        filter.states.add(DefaultState.PAUSED.name());
        return serviceCallService.getServiceCallFinder(filter);
    }
}
