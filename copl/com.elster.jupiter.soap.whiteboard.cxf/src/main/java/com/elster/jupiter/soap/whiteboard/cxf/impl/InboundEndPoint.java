package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.InboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.SoapProviderSupportFactory;
import com.elster.jupiter.util.osgi.ContextClassLoaderResource;

import org.apache.cxf.annotations.SchemaValidation;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.feature.validation.SchemaValidationFeature;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.transport.common.gzip.GZIPFeature;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bvn on 5/10/16.
 */
public final class InboundEndPoint implements ManagedEndpoint {
    private final InboundEndPointProvider endPointProvider;
    private final SoapProviderSupportFactory soapProviderSupportFactory;

    private Server endpoint;

    public InboundEndPoint(InboundEndPointProvider endPointProvider, SoapProviderSupportFactory soapProviderSupportFactory) {
        this.endPointProvider = endPointProvider;
        this.soapProviderSupportFactory = soapProviderSupportFactory;
    }

    @Override
    public void publish(EndPointConfiguration endPointConfiguration) {
        try (ContextClassLoaderResource ctx = soapProviderSupportFactory.create()) {
            List<Feature> features = new ArrayList<>();
            if (endPointConfiguration.isHttpCompression()) {
                features.add(new GZIPFeature());
            }
            if (endPointConfiguration.isSchemaValidation()) {
                features.add(new SchemaValidationFeature(operationInfo -> SchemaValidation.SchemaValidationType.BOTH));
            }
            Object implementor = endPointProvider.get();
            JaxWsServerFactoryBean svrFactory = new JaxWsServerFactoryBean();
            svrFactory.setAddress(endPointConfiguration.getUrl());
            svrFactory.setServiceBean(implementor);
            svrFactory.setFeatures(features);
            endpoint = svrFactory.create();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void stop() {
        if (endpoint != null) {
            endpoint.stop();
            endpoint = null;
        }
    }

    @Override
    public boolean isInbound() {
        return true;
    }
}
