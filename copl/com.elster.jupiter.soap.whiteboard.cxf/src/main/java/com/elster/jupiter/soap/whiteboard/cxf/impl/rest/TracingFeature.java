/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl.rest;


import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

import com.google.inject.Inject;
import org.apache.commons.io.output.TeeOutputStream;
import org.glassfish.jersey.filter.LoggingFilter;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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

    @Inject
    public TracingFeature() {
    }

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
            featureContext.register(new UnzippingLoggingFilter(logger));
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

    class UnzippingLoggingFilter extends LoggingFilter {

        private final Logger logger;

        public UnzippingLoggingFilter(Logger logger) {
            super(logger, true);
            this.logger = logger;
        }

        @Override
        public void aroundWriteTo(WriterInterceptorContext writerInterceptorContext) throws
                IOException,
                WebApplicationException {
            if (writerInterceptorContext != null) {
                ByteArrayOutputStream stream = (ByteArrayOutputStream) writerInterceptorContext.getProperty(GZIPWriterFilterInterceptor.ENTITY_LOGGER_PROPERTY);
                if (stream == null) {
                    //it means the GZIPWriter is not hooked in.
                    stream = new ByteArrayOutputStream();

                    final OutputStream outputStream = writerInterceptorContext.getOutputStream();
                    writerInterceptorContext.setOutputStream(new TeeOutputStream(outputStream, stream));
                }
                writerInterceptorContext.proceed();
                if (logger != null) {
                    logger.info(stream.toString());
                }
            }
        }
    }

}
