/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl.soap;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.InboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.InboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceProtocol;
import com.elster.jupiter.soap.whiteboard.cxf.impl.EndPointFactoryImpl;
import com.elster.jupiter.soap.whiteboard.cxf.impl.ManagedEndpoint;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by bvn on 5/11/16.
 */
public class InboundSoapEndPointFactoryImpl extends EndPointFactoryImpl<InboundSoapEndPointProvider> {

    private final Provider<InboundSoapEndPoint> inboundSoapEndPointProvider;

    @Inject
    public InboundSoapEndPointFactoryImpl(Provider<InboundSoapEndPoint> inboundSoapEndPointProvider) {
        this.inboundSoapEndPointProvider = inboundSoapEndPointProvider;
    }

    @Override
    public ManagedEndpoint createEndpoint(EndPointConfiguration endPointConfiguration) {
        return inboundSoapEndPointProvider.get()
                .init(super.getEndPointProvider(), (InboundEndPointConfiguration) endPointConfiguration);
    }

    @Override
    public boolean isInbound() {
        return true;
    }

    @Override
    public WebServiceProtocol getProtocol() {
        return WebServiceProtocol.SOAP;
    }

}
