/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithDisplayValueInfo;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Path("/loglevels")
public class LogLevelResource {

    private final Thesaurus thesaurus;

    @Inject
    public LogLevelResource(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getLogLevels(@BeanParam JsonQueryParameters queryParameters) {
        LogLevelUtils logLevelUtils = new LogLevelUtils(this.thesaurus);
        List<IdWithDisplayValueInfo<Integer>> logLevels =
                Arrays.stream(logLevelUtils.getUsedLogLevels())
                .map(logLevel -> new IdWithDisplayValueInfo<>(logLevel.intValue(), logLevelUtils.getTranslation(logLevel, this.thesaurus)))
                .collect(toList());
        return PagedInfoList.fromCompleteList("logLevels", logLevels, queryParameters);
    }
}
