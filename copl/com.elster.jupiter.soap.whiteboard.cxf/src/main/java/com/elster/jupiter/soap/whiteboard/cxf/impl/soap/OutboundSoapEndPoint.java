/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl.soap;

import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointAuthentication;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.SoapProviderSupportFactory;
import com.elster.jupiter.soap.whiteboard.cxf.impl.ManagedEndpoint;
import com.elster.jupiter.util.osgi.ContextClassLoaderResource;

import org.apache.cxf.annotations.SchemaValidation;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.feature.validation.SchemaValidationFeature;
import org.apache.cxf.transport.common.gzip.GZIPFeature;
import org.apache.cxf.transport.http.HttpConduitConfig;
import org.apache.cxf.transport.http.HttpConduitFeature;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This endpoint manager knows how to set up and tear down an outbound SOAP endpoint. To allow access to the remote server,
 * an OSGI-service is registered, making the outbound service available as OSGi service (java interface)
 * Features are added as configured on the endpoint configuration.
 * The actually registered service is cached to allow tear-down.
 */
public final class OutboundSoapEndPoint implements ManagedEndpoint {
    private static final Logger LOGGER = Logger.getLogger(OutboundSoapEndPoint.class.getSimpleName());

    private final BundleContext bundleContext;
    private final SoapProviderSupportFactory soapProviderSupportFactory;
    private final Provider<AccessLogFeature> accessLogFeatureProvider;
    private final String logDirectory;

    private OutboundSoapEndPointProvider endPointProvider;
    private OutboundEndPointConfiguration endPointConfiguration;
    private ServiceRegistration<?> serviceRegistration;
    private TracingFeature tracingFeature;

    @Inject
    public OutboundSoapEndPoint(BundleContext bundleContext, SoapProviderSupportFactory soapProviderSupportFactory,
                                Provider<AccessLogFeature> accessLogFeatureProvider,
                                @Named("LogDirectory") String logDirectory) {
        this.bundleContext = bundleContext;
        this.soapProviderSupportFactory = soapProviderSupportFactory;
        this.accessLogFeatureProvider = accessLogFeatureProvider;
        this.logDirectory = logDirectory;
    }

    OutboundSoapEndPoint init(OutboundSoapEndPointProvider endPointProvider, OutboundEndPointConfiguration endPointConfiguration) {
        this.endPointProvider = endPointProvider;
        this.endPointConfiguration = endPointConfiguration;
        return this;
    }

    @Override
    public void publish() {
        if (isPublished()) {
            throw new IllegalStateException("Service already published");
        }
        try (ContextClassLoaderResource ctx = soapProviderSupportFactory.create()) {
            List<WebServiceFeature> features = new ArrayList<>();
            if (endPointConfiguration.isHttpCompression()) {
                features.add(new GZIPFeature());
            }
            if (endPointConfiguration.isSchemaValidation()) {
                features.add(new SchemaValidationFeature(operationInfo -> SchemaValidation.SchemaValidationType.OUT));
            }
            features.add(accessLogFeatureProvider.get().init(endPointConfiguration));
            if (endPointConfiguration.isTracing()) {
                tracingFeature = new TracingFeature(logDirectory, endPointConfiguration.getTraceFile());
                features.add(tracingFeature);
            }
            if (EndPointAuthentication.BASIC_AUTHENTICATION.equals(endPointConfiguration.getAuthenticationMethod())) {
                HttpConduitFeature authFeature = new HttpConduitFeature();
                HttpConduitConfig authConfig = new HttpConduitConfig();
                AuthorizationPolicy authPolicy = new AuthorizationPolicy();
                authPolicy.setUserName(endPointConfiguration.getUsername());
                authPolicy.setPassword(endPointConfiguration.getPassword());
                authConfig.setAuthorizationPolicy(authPolicy);
                authFeature.setConduitConfig(authConfig);
                features.add(authFeature);
            }
            Service service = Service.create(new URL(endPointConfiguration.getUrl()), endPointProvider.get().getServiceName());
            createAndRegisterPort(service, endPointProvider.getService(), features);
        } catch (MalformedURLException | WebServiceException e) {
            endPointConfiguration.log("Failed to publish endpoint", e);
            LOGGER.log(Level.SEVERE, "Failed to publish endpoint: " + e.getMessage(), e);
        }
    }

    private <T> void createAndRegisterPort(Service service, Class<T> portType, Collection<WebServiceFeature> features) {
        T port = service.getPort(portType, features.toArray(new WebServiceFeature[features.size()]));
        Hashtable<String, Object> dict = new Hashtable<>();
        dict.put(AbstractOutboundEndPointProvider.ENDPOINT_CONFIGURATION_ID_PROPERTY, endPointConfiguration.getId());
        dict.put(AbstractOutboundEndPointProvider.URL_PROPERTY, endPointConfiguration.getUrl());
        serviceRegistration = bundleContext.registerService(portType, port, dict);
    }

    @Override
    public void stop() {
        if (isPublished()) {
            serviceRegistration.unregister();
            serviceRegistration = null;
            if (tracingFeature != null) {
                tracingFeature.close();
                tracingFeature = null;
            }
        } else {
            LOGGER.log(Level.SEVERE, "Service already stopped");  // CONM-252
          //  throw new IllegalStateException("Service already stopped");
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
