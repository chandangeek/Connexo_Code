package com.elster.jupiter.soap.whiteboard.cxf.impl.rest;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

import javax.inject.Inject;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * This feature will add a logger to the affected Configurable (ResourceConfig or Client) for rest web service endpoints.
 * The logger will log basic information about calls to the REST web service end point
 */
public class AccessLogFeature implements Feature {

    private final AccessLogger accessLogger;

    @Inject
    public AccessLogFeature(AccessLogger accessLogger) {
        this.accessLogger = accessLogger;
    }

    AccessLogFeature init(EndPointConfiguration endPointConfiguration) {
        accessLogger.init(endPointConfiguration);
        return this;
    }

    @Override
    public boolean configure(FeatureContext featureContext) {
        featureContext.register(accessLogger);
        return true;
    }


}
