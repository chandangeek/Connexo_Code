package com.energyict.mdc.rest.impl;

import com.energyict.mdc.common.Environment;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

public class CloseDatabaseEventListener implements ApplicationEventListener {

    @Override
    public void onEvent(ApplicationEvent event) {
        // Nothing to do
    }

    @Override
    public RequestEventListener onRequest(RequestEvent requestEvent) {
        return new RequestEventListener() {

            @Override
            public void onEvent(final RequestEvent event) {
                if (event.getType().equals(RequestEvent.Type.RESOURCE_METHOD_FINISHED)) {
                    System.out.println("Auto-Close DB connection");
                    Environment.DEFAULT.get().closeConnection();
                }
            }

        };
    }
}
