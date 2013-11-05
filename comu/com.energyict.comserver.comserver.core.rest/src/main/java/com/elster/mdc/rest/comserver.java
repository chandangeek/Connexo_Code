package com.elster.mdc.rest;

import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.servers.ComServer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/comserver")
public class comserver {

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<String> getComServers() {
        Set<String> comservers = new HashSet<>();
        for (ComServer comServer : ManagerFactory.getCurrent().getComServerFactory().findAll()) {
            comservers.add(comServer.getName());
        }
        return comservers;
    }

}
