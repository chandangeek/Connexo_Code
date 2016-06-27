package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundEndPointProvider;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import javax.inject.Inject;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceFeature;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bvn on 5/10/16.
 */
public final class OutboundEndPoint implements ManagedEndpoint {
    private OutboundEndPointProvider endPointProvider;
    private EndPointConfiguration endPointConfiguration;
    private final BundleContext bundleContext;

    private ServiceRegistration<?> serviceRegistration;

    @Inject
    public OutboundEndPoint(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    OutboundEndPoint init(OutboundEndPointProvider endPointProvider, OutboundEndPointConfiguration endPointConfiguration) {
        this.endPointProvider = endPointProvider;
        this.endPointConfiguration = endPointConfiguration;
        return this;
    }

    @Override
    public void publish() {
        if (this.isPublished()) {
            throw new IllegalStateException("Service already published");
        }
        List<WebServiceFeature> features = new ArrayList<>();
        Service providedService = endPointProvider.get();
        serviceRegistration = bundleContext.registerService(
                endPointProvider.getService(),
                providedService.getPort(endPointProvider.getService(), features.toArray(new WebServiceFeature[features.size()])),
                null);
    }

    @Override
    public void stop() {
        if (this.isPublished()) {
            serviceRegistration.unregister();
            serviceRegistration = null;
        } else {
            throw new IllegalStateException("Service already stopped");
        }
    }

    @Override
    public boolean isInbound() {
        return false;
    }

    @Override
    public boolean isPublished() {
        return this.serviceRegistration != null;
    }
}
