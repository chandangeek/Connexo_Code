package com.elster.jupiter.soap.whiteboard.cxf.impl.rest;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

import javax.inject.Inject;

/**
 * Created by bvn on 7/20/16.
 */
public class AccessLogger implements ApplicationEventListener {

    private final LoggingRequestEventListener loggingRequestEventListener;

    @Inject
    public AccessLogger(LoggingRequestEventListener loggingRequestEventListener) {
        this.loggingRequestEventListener = loggingRequestEventListener;
    }

    public AccessLogger init(EndPointConfiguration endPointConfiguration) {
        this.loggingRequestEventListener.init(endPointConfiguration);
        return this;
    }

    @Override
    public void onEvent(ApplicationEvent applicationEvent) {

    }

    @Override
    public RequestEventListener onRequest(RequestEvent requestEvent) {
        return loggingRequestEventListener;
    }

}
