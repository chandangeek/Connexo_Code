package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/comportpools")
public class ComPortPoolResource {

    private final EngineModelService engineModelService;
    private final ProtocolPluggableService protocolPluggableService;

    @Inject
    public ComPortPoolResource(EngineModelService engineModelService, ProtocolPluggableService protocolPluggableService) {
        this.engineModelService = engineModelService;
        this.protocolPluggableService = protocolPluggableService;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ComPortPoolInfo getComPortPool(@PathParam("id") int id) {
        Optional<ComPortPool> comPortPool = Optional.fromNullable(engineModelService.findComPortPool(id));
        if (comPortPool.isPresent()) {
            return ComPortPoolInfoFactory.asInfo(comPortPool.get(), engineModelService);
        }

        throw new WebApplicationException("No ComPortPool with id "+id, Response.status(Response.Status.NOT_FOUND).entity("No ComPortPool with id "+id).build());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getAllComPortPools(@BeanParam QueryParameters queryParameters) {
        List<? super ComPortPoolInfo> comPortPoolInfos = new ArrayList<>();
        List<ComPortPool> allComPortPools = engineModelService.findAllComPortPools();
        for (ComPortPool comPortPool : allComPortPools) {
            comPortPoolInfos.add(ComPortPoolInfoFactory.asInfo(comPortPool, engineModelService));
        }
        return PagedInfoList.asJson("data", comPortPoolInfos, queryParameters);
    }

    @DELETE
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteComPortPool(@PathParam("id") int id) {
        Optional<ComPortPool> comPortPool = Optional.fromNullable(engineModelService.findComPortPool(id));
        if (!comPortPool.isPresent()) {
            return Response.status(Response.Status.NOT_FOUND).entity("No ComPortPool with id " + id).build();
        }
        comPortPool.get().delete();
        return Response.noContent().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createComPortPool(ComPortPoolInfo<? super ComPortPool> comPortPoolInfo) {
        ComPortPool comPortPool = comPortPoolInfo.writeTo(comPortPoolInfo.createNew(engineModelService), protocolPluggableService);
        comPortPool.save();
        comPortPoolInfo.handlePools(comPortPool, engineModelService);
        return Response.status(Response.Status.CREATED).entity(ComPortPoolInfoFactory.asInfo(comPortPool, engineModelService)).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ComPortPoolInfo updateComPortPool(@PathParam("id") int id, ComPortPoolInfo<? super ComPortPool> comPortPoolInfo) {
        Optional<ComPortPool> comPortPool = Optional.fromNullable(engineModelService.findComPortPool(id));
        if (!comPortPool.isPresent()) {
            throw new WebApplicationException("No ComPortPool with id " + id, Response.status(Response.Status.NOT_FOUND).entity("No ComPortPool with id " + id).build());
        }
        comPortPoolInfo.writeTo(comPortPool.get(), protocolPluggableService);
        comPortPoolInfo.handlePools(comPortPool.get(), engineModelService);
        comPortPool.get().save();
        return ComPortPoolInfoFactory.asInfo(comPortPool.get(), engineModelService);
    }

}
