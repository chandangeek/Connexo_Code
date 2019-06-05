/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl.soap;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;

/**
 * This is a response-fault interceptor, however, depending on the direction of the webservice, must be connected as
 * Out or In interceptor in the appropriate stream
 * Created by bvn on 6/24/16.
 */
public class EndPointAccessFaultInterceptor extends AbstractEndPointInterceptor {
    private final WebServicesService webServicesService;

    public EndPointAccessFaultInterceptor(EndPointConfiguration endPointConfiguration, WebServicesService webServicesService) {
        super(endPointConfiguration, endPointConfiguration.isInbound() ? Phase.PRE_STREAM : Phase.RECEIVE);
        this.webServicesService = webServicesService;
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        if (isForInboundService()) { // occurrences for outbound services are processed in AbstractOutboundEndPointProvider
            try {
                webServicesService.failOccurrence(new Exception(message.getContent(Exception.class).getCause()));
            } catch (IllegalStateException e) {
                // means occurrence has already been failed and removed from context; so just ignore
            }
        }
    }
}
