package com.elster.jupiter.usagepoint.lifecycle.rest.impl;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointLifeCycleInfo;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointLifeCycleInfoFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

@Path("/lifecycle")
public class UsagePointLifeCycleResource {
    private final UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService;
    private final Provider<UsagePointLifeCycleStatesResource> statesResourceProvider;
    private final Provider<UsagePointLifeCycleTransitionsResource> transitionsResourceProvider;
    private final UsagePointLifeCycleInfoFactory lifeCycleInfoFactory;

    @Inject
    public UsagePointLifeCycleResource(UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService,
                                       Provider<UsagePointLifeCycleStatesResource> statesResourceProvider,
                                       Provider<UsagePointLifeCycleTransitionsResource> transitionsResourceProvider,
                                       UsagePointLifeCycleInfoFactory lifeCycleInfoFactory) {
        this.usagePointLifeCycleConfigurationService = usagePointLifeCycleConfigurationService;
        this.statesResourceProvider = statesResourceProvider;
        this.transitionsResourceProvider = transitionsResourceProvider;
        this.lifeCycleInfoFactory = lifeCycleInfoFactory;
    }

    @Path("/states")
    public UsagePointLifeCycleStatesResource getStates() {
        return this.statesResourceProvider.get();
    }

    @Path("/transitions")
    public UsagePointLifeCycleTransitionsResource getTransitions() {
        return this.transitionsResourceProvider.get();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getAllLifeCycles(@BeanParam JsonQueryParameters queryParameters) {
        List<UsagePointLifeCycleInfo> lifeCycles = this.usagePointLifeCycleConfigurationService.getUsagePointLifeCycles()
                .from(queryParameters)
                .find()
                .stream()
                .map(this.lifeCycleInfoFactory::from)
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("lifeCycles", lifeCycles, queryParameters);
    }
}
