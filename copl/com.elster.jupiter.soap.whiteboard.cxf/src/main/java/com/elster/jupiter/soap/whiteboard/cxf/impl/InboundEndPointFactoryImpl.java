package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.InboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.InboundSoapEndPointProvider;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by bvn on 5/11/16.
 */
public class InboundEndPointFactoryImpl extends EndPointFactoryImpl<InboundSoapEndPointProvider> {

    private final Provider<InboundSoapEndPoint> inboundEndPointProvider;

    @Inject
    public InboundEndPointFactoryImpl(Provider<InboundSoapEndPoint> inboundEndPointProvider) {
        this.inboundEndPointProvider = inboundEndPointProvider;
    }

    @Override
    public ManagedEndpoint createEndpoint(EndPointConfiguration endPointConfiguration) {
        return inboundEndPointProvider.get()
                .init(super.getEndPointProvider(), (InboundEndPointConfiguration) endPointConfiguration);
    }

    @Override
    public boolean isInbound() {
        return true;
    }
}
