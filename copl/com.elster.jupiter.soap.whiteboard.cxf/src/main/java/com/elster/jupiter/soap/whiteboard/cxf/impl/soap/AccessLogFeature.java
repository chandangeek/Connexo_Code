/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl.soap;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
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

    @Inject
    public AccessLogFeature(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    AccessLogFeature init(EndPointConfiguration endPointConfiguration) {
        this.endPointConfiguration = endPointConfiguration;
        return this;
    }

    @Override
    protected void initializeProvider(InterceptorProvider provider, Bus bus) {
        super.initializeProvider(provider, bus);
        EndPointAccessRequestInterceptor endPointAccessRequestInterceptor = new EndPointAccessRequestInterceptor(endPointConfiguration, transactionService);
        EndPointAccessResponseInterceptor endPointAccessResponseInterceptor = new EndPointAccessResponseInterceptor(endPointConfiguration, transactionService);
        EndPointAccessFaultInterceptor endPointAccessFaultInterceptor = new EndPointAccessFaultInterceptor(endPointConfiguration, transactionService);
        if (endPointConfiguration.isInbound()) {
            provider.getInInterceptors().add(endPointAccessRequestInterceptor);
            provider.getInFaultInterceptors().add(endPointAccessRequestInterceptor);
            provider.getOutInterceptors().add(endPointAccessResponseInterceptor);
            provider.getOutFaultInterceptors().add(endPointAccessFaultInterceptor);
        } else {
            provider.getInInterceptors().add(endPointAccessResponseInterceptor);
            provider.getInFaultInterceptors().add(endPointAccessFaultInterceptor);
            provider.getOutInterceptors().add(endPointAccessRequestInterceptor);
            provider.getOutFaultInterceptors().add(endPointAccessRequestInterceptor);
        }
    }
}
