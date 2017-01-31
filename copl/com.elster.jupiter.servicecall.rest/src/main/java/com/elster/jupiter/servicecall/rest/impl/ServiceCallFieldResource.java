/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithDisplayValueInfo;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.List;

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
    @RolesAllowed(Privileges.Constants.VIEW_SERVICE_CALL_TYPES)
    public PagedInfoList getLogLevels(@BeanParam JsonQueryParameters queryParameters) {
        List<IdWithDisplayValueInfo<String>> logLevels = Arrays.stream(LogLevel.values())
                .map(logLevel -> new IdWithDisplayValueInfo<>(logLevel.name(), logLevel.getDisplayName(this.thesaurus)))
                .collect(toList());
        return PagedInfoList.fromCompleteList("logLevels", logLevels, queryParameters);
    }

    @GET
    @Path("/states")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_SERVICE_CALLS})
    public List<IdWithDisplayValueInfo<String>> getStates(@BeanParam JsonQueryParameters queryParameters) {
        List<IdWithDisplayValueInfo<String>> states = Arrays.stream(DefaultState.values())
                .map(state -> new IdWithDisplayValueInfo<>(state.name(), state.getDisplayName(this.thesaurus)))
                .sorted((s1, s2) -> s1.displayValue.compareTo(s2.displayValue))
                .collect(toList());
        return states;
    }
}
