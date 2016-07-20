package com.elster.jupiter.soap.whiteboard.cxf.impl.rest;


import org.glassfish.jersey.filter.LoggingFilter;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Created by bvn on 7/20/16.
 */
public class TracingFeature implements Feature {
    private String fqFileName;
    private FileHandler fileHandler;

    public TracingFeature init(String logDirectory, String traceFile) {
        fqFileName = logDirectory + traceFile;
        return this;
    }

    @Override
    public boolean configure(FeatureContext featureContext) {
        try {
            Logger logger = Logger.getLogger(TracingFeature.class.getSimpleName());
            fileHandler = new FileHandler(fqFileName, true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false);
            featureContext.register(new LoggingFilter(logger, true));
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        if (fileHandler != null) {
            fileHandler.close();
        }
    }

}
