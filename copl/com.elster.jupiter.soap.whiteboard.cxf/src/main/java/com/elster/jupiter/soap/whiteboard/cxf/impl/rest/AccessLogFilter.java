package com.elster.jupiter.soap.whiteboard.cxf.impl.rest;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import java.io.IOException;

/**
 * Created by bvn on 7/20/16.
 */
public class AccessLogFilter implements ClientRequestFilter, ClientResponseFilter {

    private EndPointConfiguration endPointConfiguration;

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
