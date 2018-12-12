/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl.soap;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointAuthentication;
import com.elster.jupiter.soap.whiteboard.cxf.InboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.InboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.SoapProviderSupportFactory;
import com.elster.jupiter.soap.whiteboard.cxf.impl.ManagedEndpoint;
import com.elster.jupiter.util.osgi.ContextClassLoaderResource;

import org.apache.cxf.annotations.SchemaValidation;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.feature.validation.SchemaValidationFeature;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.transport.common.gzip.GZIPFeature;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

/**
 * This endpoint manager knows how to set up and tear down an inbound SOAP endpoint. Features are added as configured on the endpoint configuration.
 * The actually created endpoint is cached to allow tear-down.
 */
public final class InboundSoapEndPoint implements ManagedEndpoint {
    private InboundSoapEndPointProvider endPointProvider;
    private InboundEndPointConfiguration endPointConfiguration;
    private final SoapProviderSupportFactory soapProviderSupportFactory;
    private final String logDirectory;
    private final Provider<AuthorizationInInterceptor> authorizationInInterceptorProvider;
    private final Provider<AccessLogFeature> accessLogFeatureProvider;

    private Server endpoint;
    private TracingFeature tracingFeature;

    @Inject
    public InboundSoapEndPoint(SoapProviderSupportFactory soapProviderSupportFactory, @Named("LogDirectory") String logDirectory,
                               Provider<AuthorizationInInterceptor> authorizationInInterceptorProvider,
                               Provider<AccessLogFeature> accessLogFeatureProvider) {
        this.soapProviderSupportFactory = soapProviderSupportFactory;
        this.logDirectory = logDirectory;
        this.authorizationInInterceptorProvider = authorizationInInterceptorProvider;
        this.accessLogFeatureProvider = accessLogFeatureProvider;
    }

    InboundSoapEndPoint init(InboundSoapEndPointProvider endPointProvider, InboundEndPointConfiguration endPointConfiguration) {
        this.endPointProvider = endPointProvider;
        this.endPointConfiguration = endPointConfiguration;
        return this;
    }

    @Override
    public void publish() {
        if (this.isPublished()) {
            throw new IllegalStateException("Service already published");
        }
        try (ContextClassLoaderResource ctx = soapProviderSupportFactory.create()) {
            Object implementor = endPointProvider.get();
            JaxWsServerFactoryBean svrFactory = new JaxWsServerFactoryBean();
            if (endPointConfiguration.isHttpCompression()) {
                svrFactory.getFeatures().add(new GZIPFeature());
            }
            if (endPointConfiguration.isSchemaValidation()) {
                svrFactory.getFeatures()
                        .add(new SchemaValidationFeature(operationInfo -> SchemaValidation.SchemaValidationType.IN));
            }
            svrFactory.getFeatures().add(accessLogFeatureProvider.get().init(endPointConfiguration));
            svrFactory.setAddress(endPointConfiguration.getUrl());
            svrFactory.setServiceBean(implementor);
            if (endPointConfiguration.isTracing()) {
                tracingFeature = new TracingFeature(logDirectory, endPointConfiguration.getTraceFile());
                svrFactory.getFeatures().add(tracingFeature);
            }
            if (EndPointAuthentication.BASIC_AUTHENTICATION.equals(endPointConfiguration.getAuthenticationMethod())) {
                svrFactory.getInInterceptors()
                        .add(authorizationInInterceptorProvider.get().init(endPointConfiguration));
            }
            endpoint = svrFactory.create();
        } catch (Exception ex) {
            endPointConfiguration.log("Failed to publish the endpoint", ex);
        }
    }

    @Override
    public void stop() {
        if (this.isPublished()) {
            endpoint.stop();
            endpoint = null;
            if (tracingFeature != null) {
                tracingFeature.close();
                tracingFeature = null;
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
        return endpoint != null;
    }
}
