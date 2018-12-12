/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl.rest;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundRestEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceProtocol;
import com.elster.jupiter.soap.whiteboard.cxf.impl.EndPointFactoryImpl;
import com.elster.jupiter.soap.whiteboard.cxf.impl.ManagedEndpoint;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by bvn on 5/11/16.
 */
public class OutboundRestEndPointFactoryImpl extends EndPointFactoryImpl<OutboundRestEndPointProvider> {

    private final Provider<OutboundRestEndPoint> outboundEndPointProvider;

    @Inject
    public OutboundRestEndPointFactoryImpl(Provider<OutboundRestEndPoint> outboundEndPointProvider) {
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
    public WebServiceProtocol getProtocol() {
        return WebServiceProtocol.REST;
    }

}
