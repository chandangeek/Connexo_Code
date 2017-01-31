/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.impl.servicecall;

import com.elster.jupiter.soap.whiteboard.cxf.OutboundRestEndPointProvider;

import org.osgi.service.component.annotations.Component;

import javax.ws.rs.client.WebTarget;

@Component(name = "com.elster.jupiter.kore.api.impl.servicecall.CallbackOutboundEndpointProvider",
        service = {OutboundRestEndPointProvider.class},
        immediate = true,
        property = {"name=kore.api.callback"})
public class CallbackOutboundEndpointProvider implements OutboundRestEndPointProvider<UsagePointCommandCallbackWebService> {
    @Override
    public UsagePointCommandCallbackWebService get(WebTarget client) {
        return new UsagePointCommandCallbackWebService(client);
    }

    @Override
    public Class getService() {
        return UsagePointCommandCallbackWebService.class;
    }
}
