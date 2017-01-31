/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
import javax.inject.Provider;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This endpoint manager knows how to set up and tear down an outbound REST endpoint. To allow access to the remote server,
 * an OSGI-service is registered, making the outbound service available as OSGi service (java interface)
 * Features are added as configured on the endpoint configuration.
 * The actually registered service is cached to allow tear-down.
 */
public final class OutboundRestEndPoint<S> implements ManagedEndpoint {
    private final Provider<AccessLogFeature> accessLogFeatureProvider;
    private final AtomicReference<ServiceRegistration<S>> serviceRegistration = new AtomicReference<>();
    private final BundleContext bundleContext;
    private final String logDirectory;
    private final Provider<GZIPFeature> gzipFeatureProvider;
    private final Provider<TracingFeature> tracingFeatureProvider;

    private OutboundRestEndPointProvider<S> endPointProvider;
    private OutboundEndPointConfiguration endPointConfiguration;
    private Client client;
    private TracingFeature tracingFeature;

    @Inject
    public OutboundRestEndPoint(BundleContext bundleContext, @Named("LogDirectory") String logDirectory,
                                Provider<AccessLogFeature> accessLogFeatureProvider,
                                Provider<GZIPFeature> gzipFeatureProvider, Provider<TracingFeature> tracingFeatureProvider) {
        this.bundleContext = bundleContext;
        this.logDirectory = logDirectory;
        this.accessLogFeatureProvider = accessLogFeatureProvider;
        this.gzipFeatureProvider = gzipFeatureProvider;
        this.tracingFeatureProvider = tracingFeatureProvider;
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
                register(accessLogFeatureProvider.get().init(endPointConfiguration)).
                property(ClientProperties.CONNECT_TIMEOUT, DateTimeConstants.MILLIS_PER_SECOND * 5).
                property(ClientProperties.READ_TIMEOUT, DateTimeConstants.MILLIS_PER_SECOND * 2);
        if (EndPointAuthentication.BASIC_AUTHENTICATION.equals(endPointConfiguration.getAuthenticationMethod())) {
            client.register(HttpAuthenticationFeature.basic(endPointConfiguration.getUsername(), endPointConfiguration.getPassword()
                    .getBytes()));
        }
        client.register(gzipFeatureProvider.get().init(endPointConfiguration));
        tracingFeature = tracingFeatureProvider.get().init(logDirectory, endPointConfiguration);
        client.register(tracingFeature);
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
