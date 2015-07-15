package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 7/13/15.
 */
@Path("/comportpools")
public class ComPortPoolResource {
    private final EngineConfigurationService engineConfigurationService;
    private final ComPortPoolInfoFactory comPortPoolFactory;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public ComPortPoolResource(EngineConfigurationService engineConfigurationService, ComPortPoolInfoFactory comPortPoolFactory, ExceptionFactory exceptionFactory) {
        this.engineConfigurationService = engineConfigurationService;
        this.comPortPoolFactory = comPortPoolFactory;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response getComPortPools(@BeanParam JsonQueryParameters queryParameters, @Context UriInfo uriInfo, @BeanParam FieldSelection fieldSelection) {
        List<ComPortPoolInfo> page = ListPager.
                of(engineConfigurationService.findAllComPortPools()).
                from(queryParameters).stream().
                map(cpp -> comPortPoolFactory.asHypermedia(cpp, uriInfo, fieldSelection.getFields())).
                collect(toList());
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().path(ComPortPoolResource.class);
        return Response.ok(PagedInfoList.from(page, queryParameters, uriBuilder, uriInfo)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Path("/{id}")
    public Response getComPortPool(@PathParam("id") long id, @Context UriInfo uriInfo, @BeanParam FieldSelection fieldSelection) {
        ComPortPool comPortPool = engineConfigurationService.findComPortPool(id).orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NOT_FOUND));
        return Response.ok(comPortPoolFactory.asHypermedia(comPortPool, uriInfo, fieldSelection.getFields())).build();
    }
}
