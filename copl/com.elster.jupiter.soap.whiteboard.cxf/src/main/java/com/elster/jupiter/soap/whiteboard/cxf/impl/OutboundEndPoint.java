package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.OutboundEndPointProvider;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Created by bvn on 5/10/16.
 */
public class OutboundEndPoint implements ManagedEndpoint {
    private final OutboundEndPointProvider endPointProvider;
    private final BundleContext bundleContext;

    private ServiceRegistration<?> serviceRegistration;

    public OutboundEndPoint(OutboundEndPointProvider endPointProvider, BundleContext bundleContext) {
        this.endPointProvider = endPointProvider;
        this.bundleContext = bundleContext;
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
