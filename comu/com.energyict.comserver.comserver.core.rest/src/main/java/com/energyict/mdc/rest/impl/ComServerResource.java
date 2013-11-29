package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.servers.ComServer;
import com.energyict.mdc.services.ComServerService;
import com.energyict.mdc.shadow.servers.ComServerShadow;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/comservers")
public class ComServerResource {

    private ComServerService comServerService;

    public ComServerResource(@BeanParam ComServerServiceHolder comServerServiceHolder) {
        comServerService = comServerServiceHolder.getComServerService();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ComServersInfo getComServers(@Context UriInfo uriInfo) {
        ComServersInfo comservers = new ComServersInfo();
        for (ComServer comServer : comServerService.findAll()) {
            comservers.comServers.add(ComServerInfoFactory.asInfo(comServer));
        }
        return comservers;

    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ComServerInfo getComServer(@PathParam("id") int id) {
        ComServer comServer = comServerService.find(id);
        return ComServerInfoFactory.asInfo(comServer, comServer.getComPorts());
    }

    @GET
    @Path("/{id}/comports")
    @Produces(MediaType.APPLICATION_JSON)
    public ComPortsInfo getComPortsForComServerServer(@PathParam("id") int id) {
        ComServer<ComServerShadow> comServer = (ComServer<ComServerShadow>) comServerService.find(id);
        ComPortsInfo wrapper = new ComPortsInfo();
        for (ComPort comPort : comServer.getComPorts()) {
            wrapper.comPorts.add(ComPortInfoFactory.asInfo(comPort));
        }
        return wrapper;
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteComServer(@PathParam("id") int id) {
        try {
            comServerService.deleteComServer(id);
            return Response.ok().build();
        } catch (Exception e) {
            throw new WebApplicationException(e, Response.serverError().build());
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ComServerInfo createComServer(OnlineComServerInfo comServerInfo) {
        try {
            return ComServerInfoFactory.asInfo(comServerService.create(comServerInfo.asShadow()));
        } catch (Exception e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ComServerInfo updateComServer(@PathParam("id") int id, ComServerInfo<ComServerShadow> comServerInfo) {
        try {
            ComServer<ComServerShadow> comServer = (ComServer<ComServerShadow>) comServerService.find(id);

            ComServerShadow comServerShadow = comServerInfo.writeToShadow(comServer.getShadow());
            comServer.update(comServerShadow);
            return ComServerInfoFactory.asInfo(comServer);
        } catch (Exception e) {
            throw new WebApplicationException(e, Response.serverError().build());
        }
    }


}
