/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.soap.whiteboard.cxf.impl.soap;

import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.message.Message;

import java.util.logging.Logger;

public class CustomLoggingInInterceptor extends LoggingInInterceptor {
    private final Logger logger;

    public CustomLoggingInInterceptor(Logger logger) {
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
        super.handleMessage(message);
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }
}
