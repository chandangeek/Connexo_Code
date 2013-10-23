package com.elster.jupiter.events.rest.impl;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import com.elster.jupiter.events.rest.impl.EventTypeInfos;
import com.elster.jupiter.metering.security.Privileges;

@Path("/eventtypes")
public class EventTypeResource {
	
	  @GET
	    @RolesAllowed({Privileges.BROWSE_ANY, Privileges.BROWSE_OWN})
	    @Path("/readingtypes")
	    @Produces(MediaType.APPLICATION_JSON)
	    public EventTypeInfos getReadingTypes(@PathParam("id") long id, @Context SecurityContext securityContext) {

	    }

}
