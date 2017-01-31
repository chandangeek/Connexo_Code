/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl.rest;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;

import javax.inject.Inject;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import java.io.IOException;

/**
 * Request/Response filter allows logging basic access information for an outbound rest web service
 */
public class AccessLogFilter implements ClientRequestFilter, ClientResponseFilter {

    private EndPointConfiguration endPointConfiguration;

    @Inject
    public AccessLogFilter() {
    }

    AccessLogFilter init(EndPointConfiguration endPointConfiguration) {
        this.endPointConfiguration = endPointConfiguration;
        return this;
    }

    @Override
    public void filter(ClientRequestContext clientRequestContext) throws IOException {
        endPointConfiguration.log(LogLevel.INFO, "Request received");
    }

    @Override
    public void filter(ClientRequestContext clientRequestContext, ClientResponseContext clientResponseContext) throws
            IOException {
        if (clientResponseContext.getStatus() >= 200 && clientResponseContext.getStatus() < 300) {
            endPointConfiguration.log(LogLevel.INFO, "Request completed successfully");
        } else {
            String msg = "Request failed (HTTP " + clientResponseContext.getStatus() + ")";
            endPointConfiguration.log(LogLevel.SEVERE, msg);
        }
    }
}
