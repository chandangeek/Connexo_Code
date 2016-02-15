package com.elster.jupiter.servicecall.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithDisplayValueInfo;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.servicecall.LogLevel;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Path("/field")
public class ServiceCallFieldResource {
    private final Thesaurus thesaurus;

    @Inject
    public ServiceCallFieldResource(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @GET
    @Path("/loglevels")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getLogLevels(@BeanParam JsonQueryParameters queryParameters) {
        List<IdWithDisplayValueInfo<String>> logLevels = Arrays.stream(LogLevel.values())
                .map(logLevel -> new IdWithDisplayValueInfo<>(logLevel.name(), logLevel.getDisplayName(this.thesaurus)))
                .sorted(Comparator.comparing(ll -> ll.displayValue))
                .collect(toList());
        return PagedInfoList.fromCompleteList("logLevels", logLevels, queryParameters);
    }
}
