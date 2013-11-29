package com.energyict.mdc.rest.impl;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.ports.ComPortPool;
import com.energyict.mdc.services.ComPortPoolService;
import com.energyict.mdc.shadow.ports.ComPortPoolShadow;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/comportpools")
public class ComPortPoolResource {

    private final ComPortPoolService comPortPoolService;

    public ComPortPoolResource(@BeanParam ComPortPoolServiceHolder comPortPoolServiceHolder) {
        try {
            this.comPortPoolService = comPortPoolServiceHolder.getComPortPoolService();
        } finally {
            Environment.DEFAULT.get().closeConnection();
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ComPortPoolInfo getComPortPool(@PathParam("id") int id) {
        try {
            return ComPortPoolInfoFactory.asInfo(comPortPoolService.find(id));
        } finally {
            Environment.DEFAULT.get().closeConnection();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Object getAllComPortPools() {
        try {
            final ComPortPoolsInfo infos = new ComPortPoolsInfo();
            for (ComPortPool comPortPool : comPortPoolService.findAll()) {
                infos.comPortPools.add(ComPortPoolInfoFactory.asInfo(comPortPool));
            }
            return infos;
        } finally {
            Environment.DEFAULT.get().closeConnection();
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ComPortPoolInfo updateComPortPool(@PathParam("id") int id, ComPortPoolInfo<ComPortPoolShadow> comPortPoolInfo) {
        try {
            ComPortPool<ComPortPoolShadow> comPortPool = comPortPoolService.find(id);
            if (comPortPool==null) {
                throw new WebApplicationException("No ComPortPool with id "+id, Response.Status.INTERNAL_SERVER_ERROR);
            }
            comPortPool.update(comPortPoolInfo.asShadow());
            return ComPortPoolInfoFactory.asInfo(comPortPool);
        } catch (Exception e) {
            throw new WebApplicationException("Failed to update ComPortPool", e, Response.Status.INTERNAL_SERVER_ERROR);
        } finally {
            Environment.DEFAULT.get().closeConnection();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ComPortPoolInfo createComPortPool(ComPortPoolInfo<ComPortPoolShadow> comPortPoolInfo) {
        try {
            return ComPortPoolInfoFactory.asInfo(comPortPoolService.createComPortPool(comPortPoolInfo.asShadow()));
        } catch (Exception e) {
            throw new WebApplicationException("Failed to update ComPortPool", e, Response.Status.INTERNAL_SERVER_ERROR);
        } finally {
            Environment.DEFAULT.get().closeConnection();
        }
    }

}
