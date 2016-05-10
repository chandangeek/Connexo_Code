package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.OutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.SoapProviderSupportFactory;

import javax.xml.ws.Endpoint;

/**
 * Created by bvn on 5/10/16.
 */
public class OutboundEndPoint implements ManagedEndpoint {
    private final OutboundEndPointProvider endPointProvider;
    private final SoapProviderSupportFactory soapProviderSupportFactory;

    private Endpoint endpoint;

    public OutboundEndPoint(OutboundEndPointProvider endPointProvider, SoapProviderSupportFactory soapProviderSupportFactory) {
        this.endPointProvider = endPointProvider;
        this.soapProviderSupportFactory = soapProviderSupportFactory;
    }

    @Override
    public void publish(EndPointConfiguration endPointConfiguration) {
    }

    @Override
    public void stop() {
    }
}
