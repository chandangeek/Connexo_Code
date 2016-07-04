package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.cxf.OutboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundRestEndPointProvider;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.logging.Logger;

/**
 * This endpoint manager knows how to set up and tear down an outbound REST endpoint. To allow access to the remote server,
 * an OSGI-service is registered, making the outbound service available as OSGi service (java interface)
 * Features are added as configured on the endpoint configuration.
 * The actually registered service is cached to allow tear-down.
 */
public final class OutboundRestEndPoint implements ManagedEndpoint {
    private static final Logger logger = Logger.getLogger(OutboundRestEndPoint.class.getSimpleName());

    private final BundleContext bundleContext;
    private final String logDirectory;

    private OutboundRestEndPointProvider endPointProvider;
    private OutboundEndPointConfiguration endPointConfiguration;
    private ServiceRegistration<?> serviceRegistration;

    @Inject
    public OutboundRestEndPoint(BundleContext bundleContext, @Named("LogDirectory") String logDirectory) {
        this.bundleContext = bundleContext;
        this.logDirectory = logDirectory;
    }

    OutboundRestEndPoint init(OutboundRestEndPointProvider endPointProvider, OutboundEndPointConfiguration endPointConfiguration) {
        this.endPointProvider = endPointProvider;
        this.endPointConfiguration = endPointConfiguration;
        return this;
    }

    @Override
    public void publish() {
        if (this.isPublished()) {
            throw new IllegalStateException("Service already published");
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
