package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OnlineComServer;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/comservers")
public class ComServerResource {

    private final EngineModelService engineModelService;

    @Inject
    public ComServerResource(EngineModelService engineModelService) {
        this.engineModelService = engineModelService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ComServersInfo getComServers() {
        ComServersInfo comServers = new ComServersInfo();
        for (ComServer comServer : engineModelService.findAllComServers()) {
            comServers.comServers.add(ComServerInfoFactory.asInfo(comServer));
        }
        return comServers;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ComServerInfo getComServer(@PathParam("id") int id) {
        ComServer comServer = engineModelService.findComServer(id);
        if (comServer == null) {
            throw new WebApplicationException("No ComServer with id "+id,
                Response.status(Response.Status.NOT_FOUND).build());
        }
        return ComServerInfoFactory.asInfo(comServer, comServer.getComPorts());
    }

    @GET
    @Path("/{id}/comports")
    @Produces(MediaType.APPLICATION_JSON)
    public ComPortsInfo getComPortsForComServerServer(@PathParam("id") int id) {
        ComServer comServer = engineModelService.findComServer(id);
        if (comServer == null) {
            throw new WebApplicationException("No ComServer with id "+id,
                Response.status(Response.Status.NOT_FOUND).build());
        }
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
            ComServer comServer = engineModelService.findComServer(id);
            if (comServer == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("No ComServer with id "+id).build();
            }
            comServer.delete();
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
            OnlineComServer onlineComServer = engineModelService.newOnlineComServerInstance();
            comServerInfo.writeTo(onlineComServer,engineModelService);
            onlineComServer.save();
            return ComServerInfoFactory.asInfo(onlineComServer);
        } catch (Exception e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ComServerInfo updateComServer(@PathParam("id") int id, ComServerInfo<ComServer> comServerInfo) {
        try {
            if (comServerInfo.inboundComPortInfos ==null) {
                throw new WebApplicationException("ComServer is missing list of inbound ComPorts",
                    Response.status(Response.Status.BAD_REQUEST).build());
            }
            if (comServerInfo.outboundComPortInfos ==null) {
                throw new WebApplicationException("ComServer is missing list of outbound ComPorts",
                    Response.status(Response.Status.BAD_REQUEST).build());
            }

            ComServer comServer = engineModelService.findComServer(id);
            if (comServer == null) {
                throw new WebApplicationException("No ComServer with id "+id,
                    Response.status(Response.Status.NOT_FOUND).build());
            }

            comServerInfo.writeTo(comServer,engineModelService);
            comServer.save();
            return ComServerInfoFactory.asInfo(comServer);
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(e, Response.serverError().build());
        }
    }


}
