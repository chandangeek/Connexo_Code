package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ManagerFactory;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/comports")
public class ComPortRest {

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ComPortInfo getComPort(@PathParam("id") int id) {
        return new ComPortInfo(ManagerFactory.getCurrent().getComPortFactory().find(id));
    }

}
