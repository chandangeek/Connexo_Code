/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.partners.jbpm.extension;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

@Path("/test")
public class TestResource {

    @GET
    @Path("/hello")
    @Produces("application/json")
    public String test(@Context UriInfo uriInfo){
        return "{\"Hello world\":[]}";
    }

}
