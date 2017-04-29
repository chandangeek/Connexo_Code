/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointLifeCycleStateInfo;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointLifeCycleStateInfoFactory;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

@Path("/fields")
public class MetrologyConfigFieldResource {

    private final UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService;
    private final UsagePointLifeCycleStateInfoFactory usagePointLifeCycleStateInfoFactory;

    @Inject
    public MetrologyConfigFieldResource(UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService,
                                        UsagePointLifeCycleStateInfoFactory usagePointLifeCycleStateInfoFactory) {
        this.usagePointLifeCycleConfigurationService = usagePointLifeCycleConfigurationService;
        this.usagePointLifeCycleStateInfoFactory = usagePointLifeCycleStateInfoFactory;
    }

    @GET
    @Path("/lifecyclestates")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getAllStates(@BeanParam JsonQueryParameters queryParameters) {
        List<UsagePointLifeCycleStateInfo> states = usagePointLifeCycleConfigurationService.getUsagePointLifeCycles().stream()
                .flatMap(upl -> upl.getStates().stream().map(state -> usagePointLifeCycleStateInfoFactory.from(upl, state)))
                .collect(Collectors.toList());

        return PagedInfoList.fromCompleteList("states", states, queryParameters);
    }
}
