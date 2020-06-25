/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.google.common.primitives.Ints;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.logging.LogManager;

public class TraceLogConfiguration {
    private static final String TRACE_LOG_COUNT_PROPERTY = "{0}.trace.log.count";
    private static final String TRACE_LOG_LIMIT_PROPERTY = "{0}.trace.log.limit";
    private static final String TRACE_LOG_DEFAULT_PREFIX = "default";
    private static final int DEFAULT_TRACE_LOG_COUNT = 3;
    private static final int DEFAULT_TRACE_LOG_LIMIT = 1000000000;
    private static final LogManager logManager = LogManager.getLogManager();
    private final String endPointConfigName;

    public TraceLogConfiguration(String endPointConfigName) {
        this.endPointConfigName = endPointConfigName;
    }

    private String getProperty(String template) {
        String value = logManager.getProperty(MessageFormat.format(template, endPointConfigName));
        if (value == null) {
            value = logManager.getProperty(MessageFormat.format(template, TRACE_LOG_DEFAULT_PREFIX));
        }
        return value;
    }

    public int getCount() {
        return Optional.ofNullable(getProperty(TRACE_LOG_COUNT_PROPERTY))
                .map(Ints::tryParse)
                .orElse(DEFAULT_TRACE_LOG_COUNT);
    }

    public int getLimit() {
        return Optional.ofNullable(getProperty(TRACE_LOG_LIMIT_PROPERTY))
                .map(Ints::tryParse)
                .orElse(DEFAULT_TRACE_LOG_LIMIT);
    }
}
