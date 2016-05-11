package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.InboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.SoapProviderSupportFactory;

import javax.inject.Inject;

/**
 * Created by bvn on 5/11/16.
 */
public class InboundWebServiceImpl extends WebServiceImpl<InboundEndPointProvider> {

    private final SoapProviderSupportFactory soapProviderSupportFactory;

    @Inject
    public InboundWebServiceImpl(SoapProviderSupportFactory soapProviderSupportFactory) {
        this.soapProviderSupportFactory = soapProviderSupportFactory;
    }

    @Override
    public ManagedEndpoint createEndpoint() {
        return new InboundEndPoint(super.getEndPointProvider(), soapProviderSupportFactory);
    }

    @Override
    public boolean isInbound() {
        return true;
    }
}
