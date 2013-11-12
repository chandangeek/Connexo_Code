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
        final List<Object> logLevelStrings = new ArrayList<>();
        for (ComServer.LogLevel logLevel : ComServer.LogLevel.values()) {
            final String myLogLevel = logLevel.name();
            logLevelStrings.add(new Object() {
                public String logLevel = myLogLevel;
            });
        }

        /**
         * Why the wrapped return value? JavaScript people didn't want to see a naked JSON list, had to be
         * wrapped with meaningful field name.
         */
        return new Object() {
            public List<Object> logLevels = logLevelStrings;
        };

    }

    @GET
    @Path("/timeUnit")
    public Object getTimeUnits() {
        final List<Object> timeUnitStrings = new ArrayList<>();
        int[] timeDurations = new int[] {
                TimeDuration.MILLISECONDS,
                TimeDuration.SECONDS,
                TimeDuration.MINUTES,
                TimeDuration.HOURS,
                TimeDuration.DAYS,
                TimeDuration.WEEKS,
                TimeDuration.MONTHS,
                TimeDuration.YEARS
        };

        for (final int timeDuration : timeDurations) {
            timeUnitStrings.add(new Object() {
                public String timeUnit = TimeDuration.getTimeUnitDescription(timeDuration);
            });
        }


        /**
         * Why the wrapped return value? JavaScript people didn't want to see a naked JSON list, had to be
         * wrapped with meaningful field name.
         */
        return new Object() {
            public List<Object> timeUnits = timeUnitStrings;
        };

    }
}
