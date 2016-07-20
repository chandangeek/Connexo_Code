package com.elster.jupiter.soap.whiteboard.cxf.impl.rest;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

import javax.inject.Inject;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * This feature will add a logger to the affected Configurable (ResourceConfig or Client) for rest web service endpoints.
 * The logger will log basic information about calls to the REST web service end point
 */
public class AccessLogFeature implements Feature {

    private final AccessLogger accessLogger = new AccessLogger();
    private final LoggingRequestEventListener loggingRequestEventListener;

    @Inject
    public AccessLogFeature(LoggingRequestEventListener loggingRequestEventListener) {
        this.loggingRequestEventListener = loggingRequestEventListener;
    }

    AccessLogFeature init(EndPointConfiguration endPointConfiguration) {
        this.loggingRequestEventListener.init(endPointConfiguration);
        return this;
    }

    @Override
    public boolean configure(FeatureContext featureContext) {
        featureContext.register(accessLogger);
        return true;
    }

    private class AccessLogger implements ApplicationEventListener {


        @Override
        public void onEvent(ApplicationEvent applicationEvent) {

        }

        @Override
        public RequestEventListener onRequest(RequestEvent requestEvent) {
            return loggingRequestEventListener;
        }

    }


}
