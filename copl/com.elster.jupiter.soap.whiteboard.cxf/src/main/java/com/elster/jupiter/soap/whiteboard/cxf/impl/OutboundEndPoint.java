package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.OutboundEndPointProvider;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import javax.inject.Inject;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceFeature;
import java.net.MalformedURLException;
import java.net.URL;
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

    OutboundEndPoint init(OutboundEndPointProvider endPointProvider, EndPointConfiguration endPointConfiguration) {
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
        try {
            Service service = endPointProvider.get(new URL(endPointConfiguration.getUrl()), features);
            serviceRegistration = bundleContext.registerService(
                    endPointProvider.getService(),
                    service.getPort(endPointProvider.getService()),
                    null);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
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
