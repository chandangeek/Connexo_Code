package com.energyict.mdc.rest.impl;

import com.energyict.cbo.TimeDuration;
import com.energyict.mdc.servers.ComServer;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/field")
public class FieldResource {
    @GET
    @Path("/logLevel")
    public Object getLogLevelValues() {
        final List<String> logLevelStrings = new ArrayList<>();
        for (ComServer.LogLevel logLevel : ComServer.LogLevel.values()) {
            logLevelStrings.add(logLevel.name());
        }

        /**
         * Why the wrapped return value? JavaScript people didn't want to see a naked JSON list, had to be
         * wrapped with meaningful field name.
         */
        return new Object() {
            public List<String> logLevel = logLevelStrings;
        };

    }

    @GET
    @Path("/timeUnit")
    public Object getTimeUnits() {
        final List<String> timeUnitStrings = new ArrayList<>();
        timeUnitStrings.add(TimeDuration.getTimeUnitDescription(TimeDuration.MILLISECONDS));
        timeUnitStrings.add(TimeDuration.getTimeUnitDescription(TimeDuration.SECONDS));
        timeUnitStrings.add(TimeDuration.getTimeUnitDescription(TimeDuration.MINUTES));
        timeUnitStrings.add(TimeDuration.getTimeUnitDescription(TimeDuration.HOURS));
        timeUnitStrings.add(TimeDuration.getTimeUnitDescription(TimeDuration.DAYS));
        timeUnitStrings.add(TimeDuration.getTimeUnitDescription(TimeDuration.WEEKS));
        timeUnitStrings.add(TimeDuration.getTimeUnitDescription(TimeDuration.MONTHS));
        timeUnitStrings.add(TimeDuration.getTimeUnitDescription(TimeDuration.YEARS));

        /**
         * Why the wrapped return value? JavaScript people didn't want to see a naked JSON list, had to be
         * wrapped with meaningful field name.
         */
        return new Object() {
            public List<String> timeUnits = timeUnitStrings;
        };

    }
}
