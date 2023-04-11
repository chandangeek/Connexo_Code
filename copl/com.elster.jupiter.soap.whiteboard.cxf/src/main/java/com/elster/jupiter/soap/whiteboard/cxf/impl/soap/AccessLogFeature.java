/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl.soap;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.transaction.TransactionService;

import org.apache.cxf.Bus;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.interceptor.InterceptorProvider;

import javax.inject.Inject;

/**
 * Adds basic logging on end point access. Depending on the web service direction, the interceptors need to be wired
 * reversed or straight. Interceptors themselves figure out which phase to attach to, depending on web service direction.
 */
public class AccessLogFeature extends AbstractFeature {
    private EndPointConfiguration endPointConfiguration;
    private final TransactionService transactionService;
    private final WebServicesService webServicesService;
    private final WebServiceCallOccurrenceService webServiceCallOccurrenceService;

    @Inject
    public AccessLogFeature(TransactionService transactionService, WebServicesService webServicesService, WebServiceCallOccurrenceService webServiceCallOccurrenceService) {
        this.transactionService = transactionService;
        this.webServicesService = webServicesService;
        this.webServiceCallOccurrenceService = webServiceCallOccurrenceService;
    }

    AccessLogFeature init(EndPointConfiguration endPointConfiguration) {
        this.endPointConfiguration = endPointConfiguration;
        return this;
    }

    @Override
    protected void initializeProvider(InterceptorProvider provider, Bus bus) {
        super.initializeProvider(provider, bus);
        EndPointAccessRequestInterceptor endPointAccessRequestInterceptor
                = new EndPointAccessRequestInterceptor(endPointConfiguration, transactionService, webServicesService, webServiceCallOccurrenceService);
        if (endPointConfiguration.isInbound()) {
            provider.getInInterceptors().add(endPointAccessRequestInterceptor);
            provider.getInFaultInterceptors().add(endPointAccessRequestInterceptor);
        } else {
            provider.getOutInterceptors().add(endPointAccessRequestInterceptor);
            provider.getOutFaultInterceptors().add(endPointAccessRequestInterceptor);
        }
    }
}
