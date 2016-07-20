package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.rest.util.BinderProvider;
import com.elster.jupiter.rest.util.TransactionWrapper;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointAuthentication;
import com.elster.jupiter.soap.whiteboard.cxf.InboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.InboundRestEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceProtocol;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.osgi.ContextClassLoaderResource;

import com.fasterxml.jackson.jaxrs.base.JsonMappingExceptionMapper;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.EncodingFilter;
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

    private InboundRestEndPointProvider endPointProvider;
    private InboundEndPointConfiguration endPointConfiguration;

    private Application application;

    private String alias;
    private final Provider<RestAccessLogFeature> restAccessLogFeatureProvider;

    @Inject
    public InboundRestEndPoint(@Named("LogDirectory") String logDirectory, TransactionService transactionService, HttpService httpService, Provider<BasicAuthentication> basicAuthenticationProvider, Provider<RestAccessLogFeature> restAccessLogFeatureProvider) {
        this.logDirectory = logDirectory;
        this.transactionService = transactionService;
        this.httpService = httpService;
        this.basicAuthenticationProvider = basicAuthenticationProvider;
        this.restAccessLogFeatureProvider = restAccessLogFeatureProvider;
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
        secureConfig.register(restAccessLogFeatureProvider.get().init(endPointConfiguration));
        secureConfig.register(RolesAllowedDynamicFeature.class);
        secureConfig.register(JsonMappingExceptionMapper.class);
        secureConfig.register(new TransactionWrapper(transactionService));
//        secureConfig.register(urlRewriteFilter);
        if (application instanceof BinderProvider) {
            secureConfig.register(((BinderProvider) application).getBinder());
        }
        EncodingFilter.enableFor(secureConfig, GZipEncoder.class); // TODO deflate also
        try (ContextClassLoaderResource ctx = ContextClassLoaderResource.of(application.getClass())) {
            ServletContainer container = new ServletContainer(secureConfig);
//            HttpServlet wrapper = new EventServletWrapper(new ServletWrapper(container, threadPrincipalService), this);
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
            httpService.unregister(alias);
            alias = null;
            application = null;
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
