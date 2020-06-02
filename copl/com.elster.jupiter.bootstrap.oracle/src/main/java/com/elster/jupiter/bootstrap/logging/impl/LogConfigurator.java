/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bootstrap.logging.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;
import org.osgi.service.log.admin.LoggerAdmin;
import org.osgi.util.tracker.ServiceTracker;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component(name = "com.elster.jupiter.logging", immediate = true)
public class LogConfigurator {
    private static final Logger LOGGER = Logger.getLogger(LogConfigurator.class.getName());
    private static final String FORMAT_KEY = "com.elster.jupiter.logging.format";
    private static final String LOG_LEVEL = "com.elster.jupiter.logging.root.loglevel";
    private static final String DEFAULT_FORMAT = "%5$s";
    private volatile LogService logService;
    private volatile LogHandler handler;
    private volatile ServiceTracker<LoggerAdmin, LRST> tracker;

    public LogConfigurator() {
    }

    @Reference
    public void setLogService(LogService logService) {
        this.logService = logService;
    }

    @Activate
    public void activate(BundleContext bundleContext, Map<String, Object> props) {
        String format = DEFAULT_FORMAT;
        if (props != null && props.containsKey(FORMAT_KEY)) {
            format = (String) props.get(FORMAT_KEY);
        }
        Level level = Level.WARNING;
        if (props != null && props.containsKey(LOG_LEVEL)) {
            level = Level.parse((String) (props.get(LOG_LEVEL)));
        }
        handler = new LogHandler(logService, format);
        handler.setLevel(level);
        Logger.getLogger("").addHandler(handler);

        createCustomOsgiLog(bundleContext);
    }

    @Deactivate
    public void deactivate() {
        Logger.getLogger("").removeHandler(handler);
        if (tracker != null) {
            tracker.close();
        }
    }

    private void createCustomOsgiLog(BundleContext bundleContext) {
        try {
            Logger customOsgiLogger = Logger.getLogger(CustomOsgiLogListener.class.getName());
            Arrays.stream(customOsgiLogger.getHandlers())
                    .forEach(customOsgiLogger::removeHandler);
            customOsgiLogger.setUseParentHandlers(false);
            customOsgiLogger.addHandler(new CustomOsgiLogHandler());

            tracker = new ServiceTracker<LoggerAdmin, LRST>(bundleContext, LoggerAdmin.class, null) {

                @Override
                public LRST addingService(ServiceReference<LoggerAdmin> reference) {
                    LRST lrst = new LRST(bundleContext);
                    lrst.open();

                    return lrst;
                }

                @Override
                public void removedService(ServiceReference<LoggerAdmin> reference, LRST lrst) {
                    lrst.close();
                }
            };

            tracker.open();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to create custom osgi log", ex);
        }
    }

    class LRST extends ServiceTracker<LogReaderService, Pair> {

        LRST(BundleContext context) {
            super(context, LogReaderService.class, null);
        }

        @Override
        public Pair addingService(ServiceReference<LogReaderService> reference) {

            LogReaderService logReaderService = context.getService(reference);
            CustomOsgiLogListener customOsgiLogListener = new CustomOsgiLogListener();

            Enumeration oldLog = logReaderService.getLog();

            while (oldLog.hasMoreElements()) {
                LogEntry entry = (LogEntry) (oldLog.nextElement());
                customOsgiLogListener.logged(entry);
            }

            logReaderService.addLogListener(customOsgiLogListener);

            return new Pair(logReaderService, customOsgiLogListener);
        }

        @Override
        public void removedService(ServiceReference<LogReaderService> reference, Pair pair) {
            pair.getKey().removeLogListener(pair.getValue());
        }
    }

    class Pair extends AbstractMap.SimpleEntry<LogReaderService, CustomOsgiLogListener> {
        Pair(LogReaderService logReaderService, CustomOsgiLogListener customOsgiLogListener) {
            super(logReaderService, customOsgiLogListener);
        }
    }
}
