/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl.rest;

import com.elster.jupiter.rest.util.TransactionWrapper;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointAuthentication;
import com.elster.jupiter.soap.whiteboard.cxf.InboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.InboundRestEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceProtocol;
import com.elster.jupiter.soap.whiteboard.cxf.impl.ManagedEndpoint;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.osgi.ContextClassLoaderResource;

import com.fasterxml.jackson.jaxrs.base.JsonMappingExceptionMapper;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.servlet.ServletContainer;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Application;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * This endpoint manager knows how to set up and tear down an inbound REST endpoint. Features are added as configured on the endpoint configuration.
 * The actually created application is cached to allow tear-down.
 */
public final class InboundRestEndPoint implements ManagedEndpoint {
    private final Provider<BasicAuthentication> basicAuthenticationProvider;
    private final String logDirectory;
    private final TransactionService transactionService;
    private final HttpService httpService;
    private final Provider<AccessLogFeature> accessLogFeatureProvider;
    private final Provider<GZIPFeature> gzipFeatureProvider;
    private final Provider<TracingFeature> tracingFeatureProvider;

    private InboundRestEndPointProvider endPointProvider;
    private InboundEndPointConfiguration endPointConfiguration;

    private Application application;

    private String alias;
    private TracingFeature tracingFeature;

    @Inject
    public InboundRestEndPoint(@Named("LogDirectory") String logDirectory, TransactionService transactionService,
                               HttpService httpService, Provider<BasicAuthentication> basicAuthenticationProvider,
                               Provider<AccessLogFeature> accessLogFeatureProvider, Provider<GZIPFeature> gzipFeatureProvider,
                               Provider<TracingFeature> tracingFeatureProvider) {
        this.logDirectory = logDirectory;
        this.transactionService = transactionService;
        this.httpService = httpService;
        this.basicAuthenticationProvider = basicAuthenticationProvider;
        this.accessLogFeatureProvider = accessLogFeatureProvider;
        this.gzipFeatureProvider = gzipFeatureProvider;
        this.tracingFeatureProvider = tracingFeatureProvider;
    }

    InboundRestEndPoint init(InboundRestEndPointProvider endPointProvider, InboundEndPointConfiguration endPointConfiguration) {
        this.endPointProvider = endPointProvider;
        this.endPointConfiguration = endPointConfiguration;
        return this;
    }

    @Override
    public void publish() {
        if (this.isPublished()) {
            throw new IllegalStateException("Service already published");
        }
        alias = getAlias();
        application = endPointProvider.get();
        ResourceConfig secureConfig = ResourceConfig.forApplication(Objects.requireNonNull(application));
        secureConfig.register(ObjectMapperProvider.class);
        secureConfig.register(JacksonFeature.class);
        secureConfig.register(accessLogFeatureProvider.get().init(endPointConfiguration));
        secureConfig.register(gzipFeatureProvider.get().init(endPointConfiguration));
        secureConfig.register(RolesAllowedDynamicFeature.class);
        secureConfig.register(JsonMappingExceptionMapper.class);
        secureConfig.register(new TransactionWrapper(transactionService));
        tracingFeature = tracingFeatureProvider.get().init(logDirectory, endPointConfiguration);
        secureConfig.register(tracingFeature);

        try (ContextClassLoaderResource ctx = ContextClassLoaderResource.of(application.getClass())) {
            ServletContainer container = new ServletContainer(secureConfig);
            HttpContext httpContext = EndPointAuthentication.BASIC_AUTHENTICATION.equals(endPointConfiguration.getAuthenticationMethod())
                    ? basicAuthenticationProvider.get().init(endPointConfiguration)
                    : new NoAuthentication();
            httpService.registerServlet(alias, container, null, httpContext);
        } catch (Exception e) {
            endPointConfiguration.log("Error while registering " + alias + ": " + e.getMessage(), e);
        }

    }

    private String getAlias() {
        return "/" + WebServiceProtocol.REST.path() + endPointConfiguration.getUrl();
    }

    @Override
    public void stop() {
        if (this.isPublished()) {
            application = null;
            httpService.unregister(alias);
            alias = null;
            if (tracingFeature != null) {
                tracingFeature.close();
            }
        } else {
            throw new IllegalStateException("Service already stopped");
        }
    }

    @Override
    public boolean isInbound() {
        return true;
    }

    @Override
    public boolean isPublished() {
        return application != null;
    }

    private class NoAuthentication implements HttpContext {
        @Override
        public boolean handleSecurity(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws
                IOException {
            return true;
        }

        @Override
        public URL getResource(String s) {
            return InboundRestEndPoint.class.getResource(s);
        }

        @Override
        public String getMimeType(String s) {
            return null;
        }
    }
}
