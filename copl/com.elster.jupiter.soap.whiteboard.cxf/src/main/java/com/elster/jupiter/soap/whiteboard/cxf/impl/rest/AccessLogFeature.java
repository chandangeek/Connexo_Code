/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl.rest;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * This feature will add a logger to the affected Configurable (ResourceConfig or Client) for rest web service endpoints.
 * The logger will log basic information about calls to the REST web service end point.
 */
public class AccessLogFeature implements Feature {

    private final AccessLogger accessLogger = new AccessLogger();
    private final Provider<LoggingRequestEventListener> loggingRequestEventListener;
    private final Provider<AccessLogFilter> accessLogFilter;
    private EndPointConfiguration endPointConfiguration;

    @Inject
    public AccessLogFeature(Provider<LoggingRequestEventListener> loggingRequestEventListener, Provider<AccessLogFilter> accessLogFilter) {
        this.loggingRequestEventListener = loggingRequestEventListener;
        this.accessLogFilter = accessLogFilter;
    }

    AccessLogFeature init(EndPointConfiguration endPointConfiguration) {
        this.endPointConfiguration = endPointConfiguration;
        return this;
    }

    @Override
    public boolean configure(FeatureContext featureContext) {
        if (endPointConfiguration.isInbound()) {
            featureContext.register(accessLogger);
        } else {
            featureContext.register(accessLogFilter.get().init(endPointConfiguration));
        }
        return true;
    }

    private class AccessLogger implements ApplicationEventListener {


        @Override
        public void onEvent(ApplicationEvent applicationEvent) {

        }

        @Override
        public RequestEventListener onRequest(RequestEvent requestEvent) {
            return loggingRequestEventListener.get().init(endPointConfiguration);
        }

    }


}
