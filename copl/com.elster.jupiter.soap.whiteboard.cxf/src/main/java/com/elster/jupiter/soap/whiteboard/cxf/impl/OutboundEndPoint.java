package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.SoapProviderSupportFactory;
import com.elster.jupiter.util.osgi.ContextClassLoaderResource;

import org.apache.cxf.annotations.SchemaValidation;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.feature.validation.SchemaValidationFeature;
import org.apache.cxf.transport.common.gzip.GZIPFeature;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceFeature;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bvn on 5/10/16.
 */
public final class OutboundEndPoint implements ManagedEndpoint {
    private final BundleContext bundleContext;
    private final SoapProviderSupportFactory soapProviderSupportFactory;
    private final Provider<AccessLogFeature> accessLogFeatureProvider;
    private final String logDirectory;

    private OutboundEndPointProvider endPointProvider;
    private EndPointConfiguration endPointConfiguration;
    private ServiceRegistration<?> serviceRegistration;

    @Inject
    public OutboundEndPoint(BundleContext bundleContext, SoapProviderSupportFactory soapProviderSupportFactory,
                            Provider<AccessLogFeature> accessLogFeatureProvider,
                            @Named("LogDirectory") String logDirectory) {
        this.bundleContext = bundleContext;
        this.soapProviderSupportFactory = soapProviderSupportFactory;
        this.accessLogFeatureProvider = accessLogFeatureProvider;
        this.logDirectory = logDirectory;
    }

    OutboundEndPoint init(OutboundEndPointProvider endPointProvider, OutboundEndPointConfiguration endPointConfiguration) {
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
            List<WebServiceFeature> features = new ArrayList<>();
            if (endPointConfiguration.isHttpCompression()) {
                features.add(new GZIPFeature());
            }
            if (endPointConfiguration.isSchemaValidation()) {
                features.add(new SchemaValidationFeature(operationInfo -> SchemaValidation.SchemaValidationType.OUT));
            }
            features.add(accessLogFeatureProvider.get().init(endPointConfiguration));
            if (endPointConfiguration.isTracing()) {
                String logFile = "file:" + logDirectory + File.separator + endPointConfiguration.getTraceFile();
                features.add(new LoggingFeature(logFile, logFile));
            }
            Service service = Service.create(new URL(endPointConfiguration.getUrl()), endPointProvider.get()
                    .getServiceName());
            serviceRegistration = bundleContext.registerService(
                    endPointProvider.getService(),
                    service.getPort(endPointProvider.getService(),
                            features.toArray(new WebServiceFeature[features.size()])),
                    null);
        } catch (MalformedURLException e) {
            endPointConfiguration.log("Failed to publish endpoint", e);
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
