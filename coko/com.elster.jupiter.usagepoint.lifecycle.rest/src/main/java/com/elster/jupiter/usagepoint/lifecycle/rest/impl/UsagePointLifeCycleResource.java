package com.elster.jupiter.usagepoint.lifecycle.rest.impl;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.usagepoint.lifecycle.config.Privileges;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointLifeCycleInfo;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointLifeCycleInfoFactory;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Path("/lifecycle")
public class UsagePointLifeCycleResource {
    private final UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService;
    private final Provider<UsagePointLifeCycleStatesResource> statesResourceProvider;
    private final Provider<UsagePointLifeCycleTransitionsResource> transitionsResourceProvider;
    private final UsagePointLifeCycleInfoFactory lifeCycleInfoFactory;
    private final ResourceHelper resourceHelper;

    @Inject
    public UsagePointLifeCycleResource(UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService,
                                       Provider<UsagePointLifeCycleStatesResource> statesResourceProvider,
                                       Provider<UsagePointLifeCycleTransitionsResource> transitionsResourceProvider,
                                       UsagePointLifeCycleInfoFactory lifeCycleInfoFactory,
                                       ResourceHelper resourceHelper) {
        this.usagePointLifeCycleConfigurationService = usagePointLifeCycleConfigurationService;
        this.statesResourceProvider = statesResourceProvider;
        this.transitionsResourceProvider = transitionsResourceProvider;
        this.lifeCycleInfoFactory = lifeCycleInfoFactory;
        this.resourceHelper = resourceHelper;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.USAGE_POINT_LIFE_CYCLE_VIEW, Privileges.Constants.USAGE_POINT_LIFE_CYCLE_ADMINISTER})
    public PagedInfoList getAllLifeCycles(@BeanParam JsonQueryParameters queryParameters) {
        List<UsagePointLifeCycleInfo> lifeCycles = this.usagePointLifeCycleConfigurationService.getUsagePointLifeCycles()
                .from(queryParameters)
                .find()
                .stream()
                .map(this.lifeCycleInfoFactory::from)
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("lifeCycles", lifeCycles, queryParameters);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.USAGE_POINT_LIFE_CYCLE_ADMINISTER})
    public UsagePointLifeCycleInfo newLifeCycle(UsagePointLifeCycleInfo lifeCycleInfo) {
        UsagePointLifeCycle lifeCycle = this.usagePointLifeCycleConfigurationService.newUsagePointLifeCycle(lifeCycleInfo.name);
        return this.lifeCycleInfoFactory.fullInfo(lifeCycle);
    }

    @GET
    @Path("/{lid}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.USAGE_POINT_LIFE_CYCLE_VIEW, Privileges.Constants.USAGE_POINT_LIFE_CYCLE_ADMINISTER})
    public UsagePointLifeCycleInfo getLifeCycleById(@PathParam("lid") long lifeCycleId) {
        return this.lifeCycleInfoFactory.fullInfo(this.resourceHelper.getLifeCycleByIdOrThrowException(lifeCycleId));
    }

    @DELETE
    @Path("/{lid}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.USAGE_POINT_LIFE_CYCLE_ADMINISTER})
    public Response deleteLifeCycle(UsagePointLifeCycleInfo lifeCycleInfo) {
        UsagePointLifeCycle lifeCycle = this.resourceHelper.lockLifeCycle(lifeCycleInfo);
        lifeCycle.remove();
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @PUT
    @Path("/{lid}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.USAGE_POINT_LIFE_CYCLE_ADMINISTER})
    public UsagePointLifeCycleInfo updateLifeCycle(UsagePointLifeCycleInfo lifeCycleInfo) {
        UsagePointLifeCycle lifeCycle = this.resourceHelper.lockLifeCycle(lifeCycleInfo);
        lifeCycle.setName(lifeCycleInfo.name);
        lifeCycle.save();
        return this.lifeCycleInfoFactory.fullInfo(lifeCycle);
    }

    @POST
    @Path("/{lid}/clone")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.USAGE_POINT_LIFE_CYCLE_ADMINISTER})
    public UsagePointLifeCycleInfo cloneLifeCycle(@PathParam("lid") long lifeCycleId, UsagePointLifeCycleInfo lifeCycleInfo) {
        UsagePointLifeCycle source = this.resourceHelper.getLifeCycleByIdOrThrowException(lifeCycleId);
        return this.lifeCycleInfoFactory.fullInfo(this.usagePointLifeCycleConfigurationService.cloneUsagePointLifeCycle(lifeCycleInfo.name, source));
    }

    @Path("{lid}/states")
    public UsagePointLifeCycleStatesResource getStates() {
        return this.statesResourceProvider.get();
    }

    @Path("{lid}/transitions")
    public UsagePointLifeCycleTransitionsResource getTransitions() {
        return this.transitionsResourceProvider.get();
    }
}
