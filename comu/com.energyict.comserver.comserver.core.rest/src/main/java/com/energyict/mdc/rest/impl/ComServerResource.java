package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.servers.ComServer;
import com.energyict.mdc.servers.OnlineComServer;
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

@Path("/comservers")
public class ComServerResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ComServersInfo getComServers() {
        ComServersInfo comservers = new ComServersInfo();
        for (ComServer comServer : ManagerFactory.getCurrent().getComServerFactory().findAll()) {
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
    public ComServerInfo getComServer(@PathParam("id") int id) {
        return new OnlineComServerInfo((OnlineComServer) ManagerFactory.getCurrent().getComServerFactory().find(id));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ComServerInfo createComServer(ComServerInfo comServerInfo) {
        if (comServerInfo.comServerDescriptor.equals("OnlineComServer")) {
            try {
                return new ComServerInfo(ManagerFactory.getCurrent().getComServerFactory().createOnline(comServerInfo.asShadow()));
            } catch (Exception e) {
                throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
            }

        }
        throw new WebApplicationException(Response.Status.NOT_ACCEPTABLE);
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ComServerInfo updateComServer(@PathParam("id") int id, OnlineComServerInfo comServerInfo) {
        if (comServerInfo.comServerDescriptor.equals("OnlineComServer")) {
            try {
                OnlineComServer onlineComServer = (OnlineComServer) ManagerFactory.getCurrent().getComServerFactory().find(id);
                onlineComServer.update(comServerInfo.asShadow());
                return new ComServerInfo(onlineComServer);
            } catch (Exception e) {
                throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
            }

        }
        throw new WebApplicationException(Response.Status.NOT_ACCEPTABLE);
    }


}
