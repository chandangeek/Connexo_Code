package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.InboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.soap.whiteboard.cxf.SoapProviderSupportFactory;
import com.elster.jupiter.util.osgi.ContextClassLoaderResource;

import org.apache.cxf.annotations.SchemaValidation;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.feature.validation.SchemaValidationFeature;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.common.gzip.GZIPFeature;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * This endpoint manager knows how to set up and tear down an inbound SOAP endpoint
 */
public final class InboundEndPoint implements ManagedEndpoint {
    private InboundEndPointProvider endPointProvider;
    private EndPointConfiguration endPointConfiguration;
    private final SoapProviderSupportFactory soapProviderSupportFactory;
    private final String logDirectory;

    private Server endpoint;

    @Inject
    public InboundEndPoint(SoapProviderSupportFactory soapProviderSupportFactory, @Named("LogDirectory") String logDirectory) {
        this.soapProviderSupportFactory = soapProviderSupportFactory;
        this.logDirectory = logDirectory;
    }

    InboundEndPoint init(InboundEndPointProvider endPointProvider, EndPointConfiguration endPointConfiguration) {
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
            List<Interceptor<? extends Message>> interceptors = new ArrayList<>();
            if (endPointConfiguration.isHttpCompression()) {
                features.add(new GZIPFeature());
            }
            if (endPointConfiguration.isSchemaValidation()) {
                features.add(new SchemaValidationFeature(operationInfo -> SchemaValidation.SchemaValidationType.IN));
            }
            if (endPointConfiguration.isTracing()) {
                interceptors.add(new LoggingInInterceptor(new PrintWriter(new FileOutputStream(logDirectory + "/" + endPointConfiguration
                        .getTraceFile(), true))));
                interceptors.add(new LoggingOutInterceptor(new PrintWriter(new FileOutputStream(logDirectory + "/" + endPointConfiguration
                        .getTraceFile(), true))));
            }

            Object implementor = endPointProvider.get();
            JaxWsServerFactoryBean svrFactory = new JaxWsServerFactoryBean();
            svrFactory.setAddress(endPointConfiguration.getUrl());
            svrFactory.setServiceBean(implementor);
            svrFactory.setFeatures(features);
            svrFactory.setInInterceptors(interceptors);
            svrFactory.setOutInterceptors(interceptors);
            endpoint = svrFactory.create();
        } catch (Exception ex) {
            endPointConfiguration.log(LogLevel.SEVERE, ex.getMessage());
            ex.printStackTrace();
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
