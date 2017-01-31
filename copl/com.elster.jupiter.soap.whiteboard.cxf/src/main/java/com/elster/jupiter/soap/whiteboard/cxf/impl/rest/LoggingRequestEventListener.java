/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl.rest;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;

import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

/**
 * This event listener logger will logs basic access information about a REST endpoint
 */
public class LoggingRequestEventListener implements RequestEventListener {

    private EndPointConfiguration endPointConfiguration;

    LoggingRequestEventListener init(EndPointConfiguration endPointConfiguration) {
        this.endPointConfiguration = endPointConfiguration;
        return this;
    }

    @Override
    public void onEvent(RequestEvent requestEvent) {
        switch (requestEvent.getType()) {
            case REQUEST_MATCHED:
                endPointConfiguration.log(LogLevel.INFO, "Request received");
                break;
            case FINISHED:
                if (requestEvent.isSuccess()) {
                    endPointConfiguration.log(LogLevel.INFO, "Request completed successfully");
                } else {
                    if (requestEvent.getException() != null) {
                        String msg = "Request failed (exception: " + requestEvent.getException().getMessage() + ")";
                        endPointConfiguration.log(msg, new Exception(requestEvent.getException()));
                    } else {
                        String msg = "Request failed (HTTP " + requestEvent.getContainerResponse().getStatus() + ")";
                        endPointConfiguration.log(LogLevel.SEVERE, msg);
                    }
                }
                break;
        }
    }

}
