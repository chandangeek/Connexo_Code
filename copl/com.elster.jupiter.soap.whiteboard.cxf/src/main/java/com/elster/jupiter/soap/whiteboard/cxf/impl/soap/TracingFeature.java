/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl.soap;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.impl.LogFileHandler;
import com.elster.jupiter.soap.whiteboard.cxf.impl.TraceLogConfiguration;

import org.apache.cxf.Bus;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.interceptor.AbstractLoggingInterceptor;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.InterceptorProvider;
import org.apache.cxf.phase.AbstractPhaseInterceptor;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Stream;

/**
 * Created by dvy on 5/07/2016.
 */
public class TracingFeature extends LoggingFeature {
    private static final Logger LOG = Logger.getLogger(TracingFeature.class.getName());
    private InterceptorProvider provider;
    private EndPointConfiguration endPointConfiguration;
    private String logDirectory;

    public TracingFeature(String directory, EndPointConfiguration endPointConfiguration) {
        this.logDirectory = directory;
        this.endPointConfiguration = endPointConfiguration;
    }

    public TracingFeature(String directory, String file) {
        super(new File(directory + file).toURI().toString(), new File(directory + file).toURI().toString());
    }

    private void appendHandler(Logger logger) {
        if (logger != null) {
            TraceLogConfiguration traceLogConfiguration = new TraceLogConfiguration(endPointConfiguration.getName());
            String traceFile = Optional.of(logger).map(Logger::getHandlers)
                    .flatMap(handlers -> Stream.of(handlers).findFirst())
                    .filter(handler -> handler instanceof LogFileHandler)
                    .map(LogFileHandler.class::cast)
                    .map(LogFileHandler::getFile)
                    .orElse(null);
            if (traceFile == null || !traceFile.equals(endPointConfiguration.getTraceFile())) {
                try {
                    Handler fileHandler = new LogFileHandler(logDirectory, endPointConfiguration.getTraceFile(),
                            traceLogConfiguration.getLimit(), traceLogConfiguration.getCount(), true);
                    fileHandler.setFormatter(new SimpleFormatter());
                    fileHandler.setLevel(Level.INFO);
                    Stream.of(logger.getHandlers()).forEach(handler -> {
                        logger.removeHandler(handler);
                        handler.close();
                    });
                    logger.addHandler(fileHandler);
                    logger.setUseParentHandlers(false);
                } catch (IOException e) {
                    LOG.log(Level.SEVERE, "Cannot append Log handler", e);
                }
            }
        }
    }

    private Logger getLogger() {
        Logger logger = Logger.getLogger(endPointConfiguration.getName());
        logger.setLevel(Level.INFO);
        appendHandler(logger);
        return logger;
    }

    private void initializeProvider() {
        Logger logger = getLogger();
        AbstractPhaseInterceptor in = new CustomLoggingInInterceptor(logger);
        AbstractPhaseInterceptor out = new CustomLoggingOutInterceptor(logger);
        provider.getInInterceptors().add(in);
        provider.getInFaultInterceptors().add(in);
        provider.getOutInterceptors().add(out);
        provider.getOutFaultInterceptors().add(out);
    }

    @Override
    protected void initializeProvider(InterceptorProvider provider, Bus bus) {
        this.provider = provider;
        if (endPointConfiguration == null && logDirectory == null) {
            super.initializeProvider(provider, bus);
        } else {
            initializeProvider();
        }
    }

    /**
     * Cleans up files handles
     * We need to close the interceptors to release file locks (CXO-2268)
     */
    public void close() {
        if (provider != null) {
            closePrintWriters(provider.getInInterceptors());
            closePrintWriters(provider.getInFaultInterceptors());
            closePrintWriters(provider.getOutInterceptors());
            closePrintWriters(provider.getOutFaultInterceptors());
        }
    }

    private void closePrintWriters(Collection<Interceptor<?>> collection) {
        collection.stream()
                .filter(interceptor -> interceptor instanceof AbstractLoggingInterceptor)
                .map(AbstractLoggingInterceptor.class::cast)
                .filter(ali -> ali.getPrintWriter() != null)
                .forEach(ali -> ali.getPrintWriter().close());
    }


}
