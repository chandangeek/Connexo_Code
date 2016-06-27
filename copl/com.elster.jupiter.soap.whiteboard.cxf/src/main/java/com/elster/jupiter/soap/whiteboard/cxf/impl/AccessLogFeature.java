package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.transaction.TransactionService;

import org.apache.cxf.Bus;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.interceptor.InterceptorProvider;

import javax.inject.Inject;

/**
 * Adds basic logging on end point access.
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
        EndPointAccessInInterceptor endPointAccessInInterceptor = new EndPointAccessInInterceptor(endPointConfiguration, transactionService);
        EndPointAccessOutInterceptor endPointAccessOutInterceptor = new EndPointAccessOutInterceptor(endPointConfiguration, transactionService);
        EndPointAccessFaultOutInterceptor endPointAccessFaultOutInterceptor = new EndPointAccessFaultOutInterceptor(endPointConfiguration, transactionService);
        provider.getInInterceptors().add(endPointAccessInInterceptor);
        provider.getInFaultInterceptors().add(endPointAccessInInterceptor);
        provider.getOutInterceptors().add(endPointAccessOutInterceptor);
        provider.getOutFaultInterceptors().add(endPointAccessFaultOutInterceptor);
    }
}
