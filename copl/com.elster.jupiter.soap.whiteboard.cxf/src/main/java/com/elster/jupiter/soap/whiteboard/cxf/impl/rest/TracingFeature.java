package com.elster.jupiter.soap.whiteboard.cxf.impl.rest;


import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

import org.glassfish.jersey.filter.LoggingFilter;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Jaxrs feature will register a tracing logger to the endpoint.
 * Both incoming and outgoing traffic will be logged to a file on the local system.
 */
public class TracingFeature implements Feature {
    private FileHandler fileHandler;
    private String logDirectory;
    private EndPointConfiguration endPointConfiguration;

    public TracingFeature init(String logDirectory, EndPointConfiguration endPointConfiguration) {
        this.logDirectory = logDirectory;
        this.endPointConfiguration = endPointConfiguration;
        return this;
    }

    @Override
    public boolean configure(FeatureContext featureContext) {
        if (!endPointConfiguration.isTracing()) {
            return false;
        }
        try {
            Logger logger = Logger.getLogger(endPointConfiguration.getName());
            fileHandler = new FileHandler(logDirectory + endPointConfiguration.getTraceFile(), true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false);
            featureContext.register(new LoggingFilter(logger, true));
            return true;
        } catch (IOException e) {
            endPointConfiguration.log("Failed to enable tracing", e);
            return false;
        }
    }

    public void close() {
        if (fileHandler != null) {
            Logger logger = Logger.getLogger(endPointConfiguration.getName());
            logger.removeHandler(fileHandler);
            fileHandler.close(); // removes file lock
        }
    }

}
