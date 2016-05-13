package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.OutboundEndPointProvider;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by bvn on 5/11/16.
 */
public class OutboundEndPointFactoryImpl extends EndPointFactoryImpl<OutboundEndPointProvider> {

    private final Provider<OutboundEndPoint> outboundEndPointProvider;

    @Inject
    public OutboundEndPointFactoryImpl(Provider<OutboundEndPoint> outboundEndPointProvider) {
        this.outboundEndPointProvider = outboundEndPointProvider;
    }

    @Override
    public ManagedEndpoint createEndpoint(EndPointConfiguration endPointConfiguration) {
        return outboundEndPointProvider.get().init(super.getEndPointProvider(), endPointConfiguration);
    }

    @Override
    public boolean isInbound() {
        return false;
    }
}
