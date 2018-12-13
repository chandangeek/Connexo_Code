/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.kore.api.impl.MessageSeeds;
import com.elster.jupiter.kore.api.security.Privileges;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.rest.api.util.v1.hypermedia.FieldSelection;
import com.elster.jupiter.rest.api.util.v1.hypermedia.JsonQueryParameters;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PagedInfoList;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.PROPFIND;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Path("/usagepointlifecyclestate")
public class UsagePointLifeCycleStateResource {

    private final UsagePointLifeCycleStateInfoFactory usagePointLifeCycleStateInfoFactory;
    private final UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService;
    private final ExceptionFactory exceptionFactory;
    private final ResourceHelper resourceHelper;

    @Inject
    public UsagePointLifeCycleStateResource(UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService,
                                            UsagePointLifeCycleStateInfoFactory usagePointLifeCycleStateInfoFactory,
                                            ExceptionFactory exceptionFactory, ResourceHelper resourceHelper) {
        this.usagePointLifeCycleConfigurationService = usagePointLifeCycleConfigurationService;
        this.usagePointLifeCycleStateInfoFactory = usagePointLifeCycleStateInfoFactory;
        this.exceptionFactory = exceptionFactory;
        this.resourceHelper = resourceHelper;
    }

    /**
     * Usage point state is the current status in the device life cycle of an usage point.
     * The state defines what you can see on the usage point, which actions the usage point can execute and what you can do on the usage point.
     * @param id Unique identifier of the usage point life cycle state
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @return The usage point life cycle state as identified
     * @summary Fetch a usage point life cycle state by id
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{id}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public UsagePointLifeCycleStateInfo getUsagePointLifeCycleState(@PathParam("id") long id, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        return usagePointLifeCycleConfigurationService.findUsagePointState(id)
                .map(lc -> usagePointLifeCycleStateInfoFactory.from(lc, uriInfo, fieldSelection.getFields()))
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_USAGE_POINT_LIFECYCLE_STATE));
    }


    /**
     * Usage point state is the current status in the device life cycle of an usage point.
     * The state defines what you can see on the usage point, which actions the usage point can execute and what you can do on the usage point.
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @param queryParameters queryParameters
     * @return Paged list of usage point life cycle states
     * @summary Fetch all usage point life cycle states
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public PagedInfoList<UsagePointLifeCycleStateInfo> getAllUsagePointLifeCycleStates(@BeanParam FieldSelection fieldSelection,
                                                                    @Context UriInfo uriInfo,
                                                                    @BeanParam JsonQueryParameters queryParameters) {
        List<UsagePointLifeCycleStateInfo> infos = usagePointLifeCycleConfigurationService.getUsagePointStates()
                .stream()
                .map(lc -> usagePointLifeCycleStateInfoFactory.from(lc, uriInfo, fieldSelection.getFields()))
                .collect(toList());
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(UsagePointLifeCycleStateResource.class);

        return PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo);
    }

    /**
     * Usage point life cycle transition is the action where a usage point goes when one state to another in a usage point life cycle.
     * This transition is linked with pretransition checks and auto-actions.
     * @param usagePointMrid unique identifier of usage point
     * @param info UsagePointTransitionInfo
     * @summary Perform life cycle transition on the usage point
     */
    @Path("{usagePointMrid}/transition")
    @POST
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    @Transactional
    public Response performTransition(@PathParam("usagePointMrid") String usagePointMrid,
                                      UsagePointTransitionInfo info) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByMrid(usagePointMrid)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_USAGE_POINT));

        resourceHelper.performUsagePointTransition(usagePoint, info);

        return Response.ok().build();
    }

    /**
     * List the fields available on this type of entity.
     * <br>E.g.
     * <br>[
     * <br> "id",
     * <br> "name",
     * <br> "actions",
     * <br> "batch"
     * <br>]
     * <br>Fields in the list can be used as parameter on a GET request to the same resource, e.g.
     * <br> <i></i>GET ..../resource?fields=id,name,batch</i>
     * <br> The call above will return only the requested fields of the entity. In the absence of a field list, all fields
     * will be returned. If IDs are required in the URL for parent entities, then will be ignored when using the PROPFIND method.
     *
     * @return A list of field names that can be requested as parameter in the GET method on this entity type
     * @summary List the fields available on this type of entity
     */
    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public List<String> getFields() {
        List<String> fields = usagePointLifeCycleStateInfoFactory.getAvailableFields().stream().sorted().collect(toList());
        return fields;
    }
}
