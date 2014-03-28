package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
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
        ComPortPool comPortPool = engineModelService.findComPortPool(id);
        if (comPortPool!=null) {
            return ComPortPoolInfoFactory.asInfo(comPortPool);
        } else {
            throw new WebApplicationException("No ComPortPool with id "+id, Response.status(Response.Status.NOT_FOUND).build());
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Object getAllComPortPools(@QueryParam("limit") Integer limit,@QueryParam("start") Integer start,@QueryParam("sort") String sort) {
        List<ComPortPool> allComPortPools;
        final ComPortPoolsInfo infos = new ComPortPoolsInfo();
        if(limit!=null && start!=null){
            String[] sortStrings = {sort} ;
            allComPortPools = engineModelService.findAllComPortPools(start, limit, sortStrings);
            if(allComPortPools.size()==limit){
                infos.setCouldHaveNextPage();
            }
        } else {
            allComPortPools = engineModelService.findAllComPortPools();
        }
        for (ComPortPool comPortPool : allComPortPools) {
            infos.comPortPools.add(ComPortPoolInfoFactory.asInfo(comPortPool));
        }
        return infos;
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ComPortPoolInfo updateComPortPool(@PathParam("id") int id, ComPortPoolInfo<ComPortPool> comPortPoolInfo) {
        try {
            ComPortPool comPortPool = engineModelService.findComPortPool(id);
            if (comPortPool == null) {
                throw new WebApplicationException("No ComPortPool with id " + id, Response.Status.INTERNAL_SERVER_ERROR);
            }
            comPortPoolInfo.writeTo(comPortPool, protocolPluggableService);
            comPortPoolInfo.handlePools(comPortPool, engineModelService);
            comPortPool.save();
            return ComPortPoolInfoFactory.asInfo(comPortPool);
        } catch (Exception e) {
            throw new WebApplicationException(e.getLocalizedMessage(), e, Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getLocalizedMessage()).build());
        }
    }

    @DELETE
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteComPortPool(@PathParam("id") int id) {
       try {
            ComPortPool comPortPool = engineModelService.findComPortPool(id);
            if (comPortPool == null) {
                throw new WebApplicationException("No ComPortPool with id "+id, Response.Status.NOT_FOUND);
            }
            comPortPool.delete();
            return Response.ok().build();
        } catch (Exception e) {
            if (e.getClass().equals(WebApplicationException.class)) {
               throw e;
            }
            throw new WebApplicationException(e.getLocalizedMessage(), e, Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getLocalizedMessage()).build());
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ComPortPoolInfo createComPortPool(ComPortPoolInfo<ComPortPool> comPortPoolInfo) {
        try {
            ComPortPool comPortPool = comPortPoolInfo.writeTo(comPortPoolInfo.createNew(engineModelService), protocolPluggableService);
            comPortPool.save();
            comPortPoolInfo.handlePools(comPortPool, engineModelService);
            return ComPortPoolInfoFactory.asInfo(comPortPool);
        } catch (Exception e) {
            throw new WebApplicationException(e.getLocalizedMessage(), e, Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getLocalizedMessage()).build());
        }
    }


}
