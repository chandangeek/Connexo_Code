package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.InboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.InboundRestEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceProtocol;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by bvn on 5/11/16.
 */
public class InboundRestEndPointFactoryImpl extends EndPointFactoryImpl<InboundRestEndPointProvider> {

    private final Provider<InboundRestEndPoint> inboundRestEndPointProvider;

    @Inject
    public InboundRestEndPointFactoryImpl(Provider<InboundRestEndPoint> inboundRestEndPointProvider) {
        this.inboundRestEndPointProvider = inboundRestEndPointProvider;
    }

    @Override
    public ManagedEndpoint createEndpoint(EndPointConfiguration endPointConfiguration) {
        return inboundRestEndPointProvider.get()
                .init(super.getEndPointProvider(), (InboundEndPointConfiguration) endPointConfiguration);
    }

    @Override
    public boolean isInbound() {
        return true;
    }

    @Override
    public WebServiceProtocol getProtocol() {
        return WebServiceProtocol.REST;
    }
}
