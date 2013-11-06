package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.servers.ComServer;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/comservers")
public class ComServerRest {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ComServersInfo getComServers() {
        ComServersInfo comservers = new ComServersInfo();
        for (ComServer comServer : ManagerFactory.getCurrent().getComServerFactory().findAll()) {
            comservers.comServers.add(new ComServerInfo(comServer));
        }
        return comservers;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ComServerInfo getComServer(@PathParam("id") int id) {
        return new ComServerInfo(ManagerFactory.getCurrent().getComServerFactory().find(id));
    }

}
