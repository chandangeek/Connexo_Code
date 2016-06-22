package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointAuthentication;
import com.elster.jupiter.soap.whiteboard.cxf.InboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.InboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.soap.whiteboard.cxf.SoapProviderSupportFactory;
import com.elster.jupiter.util.osgi.ContextClassLoaderResource;

import org.apache.cxf.annotations.SchemaValidation;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.feature.validation.SchemaValidationFeature;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.transport.common.gzip.GZIPFeature;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * This endpoint manager knows how to set up and tear down an inbound SOAP endpoint
 */
public final class InboundEndPoint implements ManagedEndpoint {
    private InboundEndPointProvider endPointProvider;
    private InboundEndPointConfiguration endPointConfiguration;
    private final SoapProviderSupportFactory soapProviderSupportFactory;
    private final String logDirectory;
    private final Provider<AuthorizationInInterceptor> authorizationInInterceptorProvider;

    private Server endpoint;

    @Inject
    public InboundEndPoint(SoapProviderSupportFactory soapProviderSupportFactory, @Named("LogDirectory") String logDirectory, Provider<AuthorizationInInterceptor> authorizationInInterceptorProvider) {
        this.soapProviderSupportFactory = soapProviderSupportFactory;
        this.logDirectory = logDirectory;
        this.authorizationInInterceptorProvider = authorizationInInterceptorProvider;
    }

    InboundEndPoint init(InboundEndPointProvider endPointProvider, InboundEndPointConfiguration endPointConfiguration) {
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
            List<Feature> features = new ArrayList<>();
            if (endPointConfiguration.isHttpCompression()) {
                features.add(new GZIPFeature());
            }
            if (endPointConfiguration.isSchemaValidation()) {
                features.add(new SchemaValidationFeature(operationInfo -> SchemaValidation.SchemaValidationType.IN));
            }

            Object implementor = endPointProvider.get();
            JaxWsServerFactoryBean svrFactory = new JaxWsServerFactoryBean();
            svrFactory.setAddress(endPointConfiguration.getUrl());
            svrFactory.setServiceBean(implementor);
            svrFactory.getFeatures().add(new LoggingFeature());
            svrFactory.setFeatures(features);
            if (endPointConfiguration.isTracing()) {
                // TODO use LoggingFeature
                svrFactory.getInInterceptors()
                        .add(new LoggingInInterceptor(new PrintWriter(new FileOutputStream(logDirectory + "/" + endPointConfiguration
                                .getTraceFile(), true))));
                svrFactory.getOutInterceptors()
                        .add(new LoggingOutInterceptor(new PrintWriter(new FileOutputStream(logDirectory + "/" + endPointConfiguration
                                .getTraceFile(), true))));
            }
            if (EndPointAuthentication.BASIC_AUTHENTICATION.equals(endPointConfiguration.getAuthenticationMethod())) {
                svrFactory.getInInterceptors()
                        .add(authorizationInInterceptorProvider.get().init(endPointConfiguration));
            }
            endpoint = svrFactory.create();
        } catch (Exception ex) {
            endPointConfiguration.log(LogLevel.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void stop() {
        if (this.isPublished()) {
            endpoint.stop();
            endpoint = null;
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
