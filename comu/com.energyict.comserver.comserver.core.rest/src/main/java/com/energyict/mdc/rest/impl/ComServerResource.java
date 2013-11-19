package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.servers.ComServer;
import com.energyict.mdc.servers.OnlineComServer;
import com.energyict.mdc.services.ComServerService;
import com.energyict.mdc.shadow.servers.OnlineComServerShadow;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.glassfish.jersey.server.ResourceConfig;

@Path("/comservers")
public class ComServerResource {

    private final ComServerService comServerService;

    public ComServerResource(@Context Application application) {
//        ContextResolver<DeviceProtocolFactoryService> resolver = providers.getContextResolver(DeviceProtocolFactoryService.class, MediaType.WILDCARD_TYPE);
//        deviceProtocolFactoryService = resolver.getContext(DeviceProtocolFactoryService.class);
        comServerService = ((MdcApplication) ((ResourceConfig) application).getApplication()).getComServerService();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ComServersInfo getComServers(@Context UriInfo uriInfo) {
        ComServersInfo comservers = new ComServersInfo();
        for (ComServer comServer : comServerService.findAll()) {
            if (comServer instanceof OnlineComServer) {
                comservers.comServers.add(new OnlineComServerInfo((OnlineComServer) comServer));
            } else {
                throw new WebApplicationException("Unsupported ComServer type:"+comServer.getClass().getName(), Response.Status.INTERNAL_SERVER_ERROR);
            }
        }
        return comservers;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public OnlineComServerInfo getComServer(@PathParam("id") int id) {
        return new OnlineComServerInfo((OnlineComServer) comServerService.find(id));
    }

    @GET
    @Path("/{id}/comports")
    @Produces(MediaType.APPLICATION_JSON)
    public ComPortsInfo getComPortsForComServerServer(@PathParam("id") int id) {
        ComServer comServer = comServerService.find(id);
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
        } catch (Exception e) {
            throw new WebApplicationException(e, Response.serverError().build());
        }
        return Response.ok().build();

    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ComServerInfo createComServer(OnlineComServerInfo comServerInfo) {
//        if (comServerInfo.comServerDescriptor.equals("OnlineComServer")) {
            try {
                return new ComServerInfo(comServerService.createOnline(comServerInfo.asShadow(new OnlineComServerShadow())));
            } catch (Exception e) {
                throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
            }

//        }
//        throw new WebApplicationException(Response.Status.NOT_ACCEPTABLE);
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ComServerInfo updateComServer(@PathParam("id") int id, OnlineComServerInfo comServerInfo) {
//        if (comServerInfo.comServerDescriptor.equals("OnlineComServer")) {
            try {
                OnlineComServer comServer = (OnlineComServer) comServerService.find(id);

                return new ComServerInfo(comServerService.updateComServer(id, comServerInfo.asShadow(comServer.getShadow())));
            } catch (Exception e) {
                throw new WebApplicationException(e, Response.serverError().build());
            }

//        }
//        throw new WebApplicationException(Response.Status.NOT_ACCEPTABLE);
    }


}
