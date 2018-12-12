/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.rest.impl.comserver;


import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.engine.config.ComServer;

import com.google.common.collect.Ordering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LogLevelAdapter extends MapBasedXmlAdapter<ComServer.LogLevel> {

    public LogLevelAdapter() {
        register("", null);
        register(ComServer.LogLevel.ERROR.getNameKey(), ComServer.LogLevel.ERROR);
        register(ComServer.LogLevel.WARN.getNameKey(), ComServer.LogLevel.WARN);
        register(ComServer.LogLevel.INFO.getNameKey(), ComServer.LogLevel.INFO);
        register(ComServer.LogLevel.DEBUG.getNameKey(), ComServer.LogLevel.DEBUG);
        register(ComServer.LogLevel.TRACE.getNameKey(), ComServer.LogLevel.TRACE);
    }

    @Override
    public List<String> getClientSideValues() {
        Ordering<ComServer.LogLevel> byPriority = Ordering.explicit(
                ComServer.LogLevel.ERROR,
                ComServer.LogLevel.WARN,
                ComServer.LogLevel.INFO,
                ComServer.LogLevel.DEBUG,
                ComServer.LogLevel.TRACE
        );
        List<ComServer.LogLevel> values = byPriority.sortedCopy(Arrays.asList(ComServer.LogLevel.values()));
        List<String> result = new ArrayList<>(values.size());
        for(ComServer.LogLevel logLevel : values) {
            result.add(logLevel.getNameKey());
        }

        return result;
    }
}
