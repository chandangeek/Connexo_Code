package com.elster.jupiter.soap.whiteboard.cxf.impl.rest;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointAuthentication;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundRestEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.impl.ManagedEndpoint;

import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.joda.time.DateTimeConstants;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/**
 * This endpoint manager knows how to set up and tear down an outbound REST endpoint. To allow access to the remote server,
 * an OSGI-service is registered, making the outbound service available as OSGi service (java interface)
 * Features are added as configured on the endpoint configuration.
 * The actually registered service is cached to allow tear-down.
 */
public final class OutboundRestEndPoint<S> implements ManagedEndpoint {
    private static final Logger logger = Logger.getLogger(OutboundRestEndPoint.class.getSimpleName());

    private final BundleContext bundleContext;
    private final String logDirectory;

    private OutboundRestEndPointProvider<S> endPointProvider;
    private OutboundEndPointConfiguration endPointConfiguration;
    private final AtomicReference<ServiceRegistration<S>> serviceRegistration = new AtomicReference<>();
    private Client client;
    private TracingFeature tracingFeature;

    @Inject
    public OutboundRestEndPoint(BundleContext bundleContext, @Named("LogDirectory") String logDirectory) {
        this.bundleContext = bundleContext;
        this.logDirectory = logDirectory;
    }

    OutboundRestEndPoint init(OutboundRestEndPointProvider<S> endPointProvider, OutboundEndPointConfiguration endPointConfiguration) {
        this.endPointProvider = endPointProvider;
        this.endPointConfiguration = endPointConfiguration;
        return this;
    }

    @Override
    public void publish() {
        if (this.isPublished()) {
            throw new IllegalStateException("Service already published");
        }
        client = ClientBuilder.newClient().
                register(new JacksonFeature()).
                property(ClientProperties.CONNECT_TIMEOUT, DateTimeConstants.MILLIS_PER_SECOND * 5).
                property(ClientProperties.READ_TIMEOUT, DateTimeConstants.MILLIS_PER_SECOND * 2);
        if (EndPointAuthentication.BASIC_AUTHENTICATION.equals(endPointConfiguration.getAuthenticationMethod())) {
            client.register(HttpAuthenticationFeature.basic(endPointConfiguration.getUsername(), endPointConfiguration.getPassword()
                    .getBytes()));
        }
        if (endPointConfiguration.isTracing()) {
            try {
                tracingFeature = new TracingFeature().init(logDirectory, endPointConfiguration.getTraceFile());
                client.register(tracingFeature);
            } catch (Exception e) {
                endPointConfiguration.log("Failed to enable tracing", e);
            }
        }
        WebTarget target = client.target(endPointConfiguration.getUrl());

        S service = endPointProvider.get(target);
        Hashtable<String, String> dict = new Hashtable<>();
        dict.put("url", endPointConfiguration.getUrl());
        serviceRegistration.set(bundleContext.registerService(
                endPointProvider.getService(),
                service,
                dict));

    }

    @Override
    public void stop() {
        if (this.isPublished()) {
            serviceRegistration.getAndSet(null).unregister();
            client.close();
            client = null;
            if (tracingFeature != null) {
                tracingFeature.close();
            }
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
        return this.serviceRegistration.get() != null;
    }
}
