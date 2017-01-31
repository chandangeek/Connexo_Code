/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.yellowfin.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.users.User;
import com.elster.jupiter.yellowfin.YellowfinService;
import com.elster.jupiter.yellowfin.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Path("/user")
public class YellowfinResource {

	private final YellowfinService yellowfinService;
	private final String errorMessage;


	@Inject
	private YellowfinResource(YellowfinService yellowfinService, Thesaurus thesaurus){
		this.yellowfinService = yellowfinService;
		this.errorMessage = thesaurus.getString("error.facts.unavailable", "Connexo Facts is not available.");
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
	@Path("/url")
	@RolesAllowed({Privileges.Constants.VIEW_REPORTS,Privileges.Constants.DESIGN_REPORTS})
	public YellowfinInfo getUrl() {
		YellowfinInfo info = new YellowfinInfo();
		info.token = "";
		info.url = yellowfinService.getYellowfinUrl();
		return info;
	}


	@POST
	@Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
	@Path("/login")
	@RolesAllowed({Privileges.Constants.VIEW_REPORTS,Privileges.Constants.DESIGN_REPORTS})
	public YellowfinInfo login(HttpServletResponse response, @Context SecurityContext securityContext) {
		User user = (User) securityContext.getUserPrincipal();

		String found = yellowfinService.getUser(user.getName()).
				orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorMessage).build()));

		if(found.equals("NOT_FOUND")) {
			found = yellowfinService.createUser(user.getName()).
					orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorMessage).build()));
		}
		else if(found.equals("SUCCESS")) {
			yellowfinService.logout(user.getName()).
					orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorMessage).build()));
		}

		if(found.equals("SUCCESS")) {
			String webServiceLoginToken = yellowfinService.login(user.getName()).
					orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorMessage).build()));

			YellowfinInfo info = new YellowfinInfo();
			info.token = webServiceLoginToken;
			info.url = yellowfinService.getYellowfinUrl();
			return info;
		}
		else {
			throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorMessage).build());
		}
	}
	@POST
	@Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
	@Path("/token")
	@RolesAllowed(Privileges.Constants.VIEW_REPORTS)
	public YellowfinInfo token(HttpServletResponse response, @Context SecurityContext securityContext) {
		User user = (User) securityContext.getUserPrincipal();

		String found = yellowfinService.getUser(user.getName()).
				orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorMessage).build()));

		if(found.equals("NOT_FOUND")) {
			found = yellowfinService.createUser(user.getName()).
					orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorMessage).build()));
		}

		if(found.equals("SUCCESS")) {
			String webServiceLoginToken = yellowfinService.login(user.getName()).
					orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorMessage).build()));

			YellowfinInfo info = new YellowfinInfo();
			info.token = webServiceLoginToken;
			info.url = yellowfinService.getYellowfinUrl();
			return info;
		}
		else {
			throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorMessage).build());
		}
	}

}
