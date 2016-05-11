package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.InboundEndPointProvider;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by bvn on 5/11/16.
 */
public class InboundEndPointFactoryImpl extends EndPointFactoryImpl<InboundEndPointProvider> {

    private final Provider<InboundEndPoint> inboundEndPointProvider;

    @Inject
    public InboundEndPointFactoryImpl(Provider<InboundEndPoint> inboundEndPointProvider) {
        this.inboundEndPointProvider = inboundEndPointProvider;
    }

    @Override
    public ManagedEndpoint createEndpoint() {
        return inboundEndPointProvider.get().init(super.getEndPointProvider());
    }

    @Override
    public boolean isInbound() {
        return true;
    }
}
