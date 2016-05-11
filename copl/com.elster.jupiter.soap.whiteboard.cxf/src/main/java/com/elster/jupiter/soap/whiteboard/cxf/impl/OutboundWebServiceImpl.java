package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.OutboundEndPointProvider;

import org.osgi.framework.BundleContext;

import javax.inject.Inject;

/**
 * Created by bvn on 5/11/16.
 */
public class OutboundWebServiceImpl extends WebServiceImpl<OutboundEndPointProvider> {

    private final BundleContext bundleContext;

    @Inject
    public OutboundWebServiceImpl(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    public ManagedEndpoint createEndpoint() {
        return new OutboundEndPoint(super.getEndPointProvider(), bundleContext);
    }

    @Override
    public boolean isInbound() {
        return false;
    }
}
