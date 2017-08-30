/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservices.outbound.rest.issue.actions.impl;

import com.elster.jupiter.issue.share.IssueWebServiceClient;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundRestEndPointProvider;


import org.osgi.service.component.annotations.Component;

import javax.ws.rs.client.WebTarget;

/**
 * Outbound Issue action REST web service
 */
@Component(name = "com.elster.jupiter.webservices.rest.outbound.service.action.client.provider",
        service = {OutboundRestEndPointProvider.class},
        immediate = true,
        property = {"name=Issue logging"})
public class IssueLoggingActionEndpointProvider implements OutboundRestEndPointProvider {
    @Override
    public IssueWebServiceClient get(WebTarget target) {
        return new IssueLoggingClientImpl(target);
    }

    @Override
    public Class<IssueWebServiceClient> getService() {
        return IssueWebServiceClient.class;
    }
}
