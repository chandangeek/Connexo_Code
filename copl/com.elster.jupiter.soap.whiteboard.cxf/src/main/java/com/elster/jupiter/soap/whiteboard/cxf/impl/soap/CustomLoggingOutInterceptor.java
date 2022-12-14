/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.soap.whiteboard.cxf.impl.soap;

import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.message.Message;

import java.util.List;
import java.util.TreeMap;
import java.util.logging.Logger;

public class CustomLoggingOutInterceptor extends LoggingOutInterceptor {
    private final Logger logger;

    public CustomLoggingOutInterceptor(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void setOutputLocation(String pattern) {
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        Endpoint ep = message.getExchange().getEndpoint();
        if (ep != null && ep.getEndpointInfo() != null) {
            ep.getEndpointInfo().setProperty("MessageLogger", logger);
        }

        TreeMap<String, List> headers = (TreeMap<String, List>) message
                .get("org.apache.cxf.message.Message.PROTOCOL_HEADERS");

        if (headers != null) {
            List value = headers.get("Authorization");
            String authorizationHeader = value != null ? (String) value.get(0) : "";
            if (authorizationHeader.contains("Basic")) {
                value.set(0, "Basic REDACTED");
                // CONM-1574 ask for the Basic Authorization password to be removed
                // CONM-1574 Authorization=[Basic cm9vdDpyb290] -> Authorization=[Basic REDACTED]
            }
        }

        super.handleMessage(message);
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }
}
