package com.energyict.mdc.rest.impl;

import com.energyict.mdc.servers.ComServer;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/field")
public class FieldResource {
    @GET
    @Path("/logLevel")
    public Object getLogLevelValues() {
        final Set<String> logLevelStrings = new HashSet<>();
        for (ComServer.LogLevel logLevel : ComServer.LogLevel.values()) {
            logLevelStrings.add(logLevel.name());
        }
        return new Object() {
            public Set<String> logLevel = logLevelStrings;
        };
    }
}
