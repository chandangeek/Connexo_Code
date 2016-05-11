package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.OutboundEndPointProvider;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import javax.inject.Inject;

/**
 * Created by bvn on 5/10/16.
 */
public final class OutboundEndPoint implements ManagedEndpoint {
    private OutboundEndPointProvider endPointProvider;
    private final BundleContext bundleContext;

    private ServiceRegistration<?> serviceRegistration;

    @Inject
    public OutboundEndPoint(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    OutboundEndPoint init(OutboundEndPointProvider endPointProvider) {
        this.endPointProvider = endPointProvider;
        return this;
    }

    @Override
    public void publish(EndPointConfiguration endPointConfiguration) {
        serviceRegistration = bundleContext.registerService(endPointProvider.getServices(), endPointProvider
                .get(), null);
    }

    @Override
    public void stop() {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
            serviceRegistration = null;
        }
    }

    @Override
    public boolean isInbound() {
        return false;
    }
}
