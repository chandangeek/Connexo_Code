/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl.soap;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceProtocol;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.soap.whiteboard.cxf.impl.EndPointFactoryImpl;
import com.elster.jupiter.soap.whiteboard.cxf.impl.ManagedEndpoint;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.xml.ws.WebServiceException;
import java.util.Optional;

/**
 * Created by bvn on 5/11/16.
 */
public class OutboundSoapEndPointFactoryImpl extends EndPointFactoryImpl<OutboundSoapEndPointProvider> {

    private final Provider<OutboundSoapEndPoint> outboundEndPointProvider;
    private final WebServicesService webServicesService;

    @Inject
    public OutboundSoapEndPointFactoryImpl(Provider<OutboundSoapEndPoint> outboundEndPointProvider, WebServicesService webServicesService) {
        this.outboundEndPointProvider = outboundEndPointProvider;
        this.webServicesService = webServicesService;
    }

    @Override
    public ManagedEndpoint createEndpoint(EndPointConfiguration endPointConfiguration) {
        if (endPointConfiguration instanceof OutboundEndPointConfiguration){
            return outboundEndPointProvider.get()
                    .init(super.getEndPointProvider(), (OutboundEndPointConfiguration) endPointConfiguration);
        }

        Optional<EndPointConfiguration> existingEndPointConfiguration =
                webServicesService.getPublishedEndPoints()
                        .stream()
                        .filter(ep -> ep.getId()==endPointConfiguration.getId()).findFirst();

        if (existingEndPointConfiguration.isPresent()) {
            OutboundEndPointConfiguration outboundEndPointConfiguration = (OutboundEndPointConfiguration) existingEndPointConfiguration.get();
            return outboundEndPointProvider.get()
                    .init(super.getEndPointProvider(), outboundEndPointConfiguration);
        } else {
            throw new WebServiceException("Endpoint configuration "+endPointConfiguration.getName()+" not found!");
        }
    }

    @Override
    public boolean isInbound() {
        return false;
    }

    @Override
    public WebServiceProtocol getProtocol() {
        return WebServiceProtocol.SOAP;
    }

}
