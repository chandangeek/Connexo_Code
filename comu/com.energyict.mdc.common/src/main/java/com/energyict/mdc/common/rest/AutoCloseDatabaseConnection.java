package com.energyict.mdc.common.rest;

import com.energyict.mdc.common.Environment;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

/**
 * This listener automatically closes DB connection after EVERY REST call
 * This is only required as long as legacy *Factory-s are in use
 */
public class AutoCloseDatabaseConnection implements ApplicationEventListener {

    @Override
    public void onEvent(ApplicationEvent event) {
        // Nothing to do
    }

    @Override
    public RequestEventListener onRequest(RequestEvent requestEvent) {
        return new RequestEventListener() {

            @Override
            public void onEvent(final RequestEvent event) {
                switch (event.getType()) {
                case RESOURCE_METHOD_START:

                    break;
                case RESOURCE_METHOD_FINISHED:
                    Environment.DEFAULT.get().closeConnection();
                    break;
                }
            }
        };
    }
}
