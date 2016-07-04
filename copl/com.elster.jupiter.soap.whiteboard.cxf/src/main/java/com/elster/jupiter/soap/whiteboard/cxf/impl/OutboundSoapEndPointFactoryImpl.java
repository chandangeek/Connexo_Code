package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceType;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by bvn on 5/11/16.
 */
public class OutboundSoapEndPointFactoryImpl extends EndPointFactoryImpl<OutboundSoapEndPointProvider> {

    private final Provider<OutboundSoapEndPoint> outboundEndPointProvider;

    @Inject
    public OutboundSoapEndPointFactoryImpl(Provider<OutboundSoapEndPoint> outboundEndPointProvider) {
        this.outboundEndPointProvider = outboundEndPointProvider;
    }

    @Override
    public ManagedEndpoint createEndpoint(EndPointConfiguration endPointConfiguration) {
        return outboundEndPointProvider.get()
                .init(super.getEndPointProvider(), (OutboundEndPointConfiguration) endPointConfiguration);
    }

    @Override
    public boolean isInbound() {
        return false;
    }

    @Override
    public WebServiceType getType() {
        return WebServiceType.SOAP;
    }

}
