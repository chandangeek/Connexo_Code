package com.energyict.mdc.device.lifecycle.config.rest.resource;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.Privileges;
import com.energyict.mdc.device.lifecycle.config.rest.response.LifecycleInfo;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;


@Path("/devicelifecycles")
public class LifecycleResource {
    private final Thesaurus thesaurus;
    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private final ResourceHelper resourceHelper;
    private final Provider<LifeCycleStateResource> lifeCycleStateResourceProvider;
    private final Provider<LifeCycleStateTransitionsResource> lifeCycleStateTransitionsResourceProvider;

    @Inject
    public LifecycleResource(Thesaurus thesaurus, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService, ResourceHelper resourceHelper, Provider<LifeCycleStateResource> lifeCycleStateResourceProvider, Provider<LifeCycleStateTransitionsResource> lifeCycleStateTransitionsResourceProvider) {
        this.thesaurus = thesaurus;
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
        this.resourceHelper = resourceHelper;
        this.lifeCycleStateResourceProvider = lifeCycleStateResourceProvider;
        this.lifeCycleStateTransitionsResourceProvider = lifeCycleStateTransitionsResourceProvider;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE_LIFE_CYCLES})
    public PagedInfoList getDeviceLifecycles(@BeanParam QueryParameters queryParams) {
        List<LifecycleInfo> lifecycles = deviceLifeCycleConfigurationService.findAllDeviceLifeCycles().from(queryParams).stream()
                .map(LifecycleInfo::new).collect(Collectors.toList());
        return PagedInfoList.fromPagedList("deviceLifeCycles", lifecycles, queryParams);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE_LIFE_CYCLES})
    public Response getDeviceLifecycleById(@PathParam("id") Long id, @BeanParam QueryParameters queryParams) {
        DeviceLifeCycle lifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(id);
        return Response.ok(new LifecycleInfo(lifeCycle)).build();
    }

    @Path("/{cycleId}/states")
    public LifeCycleStateResource getLifeCycleStateResource() {
        return this.lifeCycleStateResourceProvider.get();
    }

    @Path("/{cycleId}/transitions")
    public LifeCycleStateTransitionsResource getLifeCycleTransitionsResource() {
        return this.lifeCycleStateTransitionsResourceProvider.get();
    }
}