package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.EndPointProvider;
import com.elster.jupiter.soap.whiteboard.SoapProviderSupportFactory;
import com.elster.jupiter.util.osgi.ContextClassLoaderResource;

import javax.xml.ws.Endpoint;

/**
 * Created by bvn on 5/10/16.
 */
public class InboundEndPoint implements ManagedEndpoint {
    private final EndPointProvider endPointProvider;
    private final SoapProviderSupportFactory soapProviderSupportFactory;

    private Endpoint endpoint;

    public InboundEndPoint(EndPointProvider endPointProvider, SoapProviderSupportFactory soapProviderSupportFactory) {
        this.endPointProvider = endPointProvider;
        this.soapProviderSupportFactory = soapProviderSupportFactory;
    }

    @Override
    public void publish(EndPointConfiguration endPointConfiguration) {
        try (ContextClassLoaderResource ctx = soapProviderSupportFactory.create()) {
            this.endpoint = javax.xml.ws.Endpoint.publish(endPointConfiguration.getUrl(), endPointProvider.get());
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
}
