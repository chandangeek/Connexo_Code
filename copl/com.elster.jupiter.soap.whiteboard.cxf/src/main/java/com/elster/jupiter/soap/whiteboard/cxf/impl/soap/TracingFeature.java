/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl.soap;

import org.apache.cxf.Bus;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.interceptor.AbstractLoggingInterceptor;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.InterceptorProvider;

import java.io.File;
import java.util.Collection;

/**
 * Created by dvy on 5/07/2016.
 */
public class TracingFeature extends LoggingFeature {
    private InterceptorProvider provider;

    public TracingFeature(String directory, String file) {
        super(new File(directory + file).toURI().toString(), new File(directory + file).toURI().toString());
    }

    @Override
    protected void initializeProvider(InterceptorProvider provider, Bus bus) {
        super.initializeProvider(provider, bus);
        this.provider = provider;
    }

    /**
     * Cleans up files handles
     * We need to close the interceptors to release file locks (CXO-2268)
     */
    public void close() {
        if (provider != null) {
            closePrintWriters(provider.getInInterceptors());
            closePrintWriters(provider.getInFaultInterceptors());
            closePrintWriters(provider.getOutInterceptors());
            closePrintWriters(provider.getOutFaultInterceptors());
        }
    }

    private void closePrintWriters(Collection<Interceptor<?>> collection) {
        collection.stream()
                .filter(interceptor -> interceptor instanceof AbstractLoggingInterceptor)
                .map(AbstractLoggingInterceptor.class::cast)
                .forEach(ali -> ali.getPrintWriter().close());
    }


}
