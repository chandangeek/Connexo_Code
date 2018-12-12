/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.rest;

import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

import javax.ws.rs.WebApplicationException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This listener writes all exceptions to a logger
 */
public class ExceptionLogger implements ApplicationEventListener {
    private static final Logger LOGGER = Logger.getLogger("ExceptionLogger");
    private final ExceptionLoggingRequestEventListener requestEventListener = new ExceptionLoggingRequestEventListener();

    @Override
    public void onEvent(ApplicationEvent event) {
        // Nothing to do
    }

    @Override
    public RequestEventListener onRequest(RequestEvent requestEvent) {
        return requestEventListener;
    }

    private class ExceptionLoggingRequestEventListener implements RequestEventListener {

        @Override
        public void onEvent(RequestEvent event) {
            switch (event.getType()) {
            case ON_EXCEPTION:
                Throwable exception = event.getException();
                if (exception !=null && !(exception instanceof WebApplicationException)) {
                    LOGGER.severe(exception.getMessage());
                    LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
                }
                break;
            }
        }
    }

}
