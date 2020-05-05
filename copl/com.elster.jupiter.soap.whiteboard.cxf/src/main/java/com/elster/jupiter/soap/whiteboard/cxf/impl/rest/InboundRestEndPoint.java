/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl.rest;

import com.elster.jupiter.rest.util.MimeTypesExt;
import com.elster.jupiter.rest.util.TransactionWrapper;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointAuthentication;
import com.elster.jupiter.soap.whiteboard.cxf.InboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.InboundRestEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceProtocol;
import com.elster.jupiter.soap.whiteboard.cxf.impl.ManagedEndpoint;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.osgi.ContextClassLoaderResource;

import com.fasterxml.jackson.jaxrs.base.JsonMappingExceptionMapper;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.servlet.ServletContainer;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.servlet.http.HttpServlet;
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
    private final Provider<OAuth2Authorization> oAuth2AuthorizationProvider;
    private final String logDirectory;
    private final TransactionService transactionService;
    private final HttpService httpService;
    private final Provider<AccessLogFeature> accessLogFeatureProvider;
    private final Provider<GZIPFeature> gzipFeatureProvider;
    private final Provider<TracingFeature> tracingFeatureProvider;
    private final ThreadPrincipalService threadPrincipalService;

    private InboundRestEndPointProvider endPointProvider;
    private InboundEndPointConfiguration endPointConfiguration;

    private Application application;

    private String alias;
    private TracingFeature tracingFeature;

    @Inject
    public InboundRestEndPoint(@Named("LogDirectory") String logDirectory, TransactionService transactionService,
                               HttpService httpService, Provider<BasicAuthentication> basicAuthenticationProvider,
                               Provider<OAuth2Authorization> oAuth2AuthorizationProvider, Provider<AccessLogFeature> accessLogFeatureProvider, Provider<GZIPFeature> gzipFeatureProvider,
                               Provider<TracingFeature> tracingFeatureProvider, ThreadPrincipalService threadPrincipalService) {
        this.logDirectory = logDirectory;
        this.transactionService = transactionService;
        this.httpService = httpService;
        this.basicAuthenticationProvider = basicAuthenticationProvider;
        this.oAuth2AuthorizationProvider = oAuth2AuthorizationProvider;
        this.accessLogFeatureProvider = accessLogFeatureProvider;
        this.gzipFeatureProvider = gzipFeatureProvider;
        this.tracingFeatureProvider = tracingFeatureProvider;
        this.threadPrincipalService = threadPrincipalService;
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
        secureConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(threadPrincipalService).to(ThreadPrincipalService.class);
            }
        });

        try (ContextClassLoaderResource ctx = ContextClassLoaderResource.of(application.getClass())) {
            ServletContainer container = new ServletContainer(secureConfig);
            HttpServlet wrapper = new ServletWrapper(container, threadPrincipalService);
            HttpContext httpContext = null;
            if(EndPointAuthentication.BASIC_AUTHENTICATION.equals(endPointConfiguration.getAuthenticationMethod())){
                httpContext = basicAuthenticationProvider.get().init(endPointConfiguration);
            } else if(EndPointAuthentication.OAUTH2_FRAMEWORK.equals(endPointConfiguration.getAuthenticationMethod())){
                httpContext = oAuth2AuthorizationProvider.get().init(endPointConfiguration);
            } else {
                httpContext = new NoAuthentication();
            }
            httpService.registerServlet(alias, wrapper, null, httpContext);
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
            httpServletResponse.addHeader("X-Content-Type-Options", "nosniff");
            httpServletResponse.setContentType("application/octet-stream");
            return true;
        }

        @Override
        public URL getResource(String s) {
            return InboundRestEndPoint.class.getResource(s);
        }

        @Override
        public String getMimeType(String s) {
            return MimeTypesExt.get().getByFile(s);
        }
    }
}
