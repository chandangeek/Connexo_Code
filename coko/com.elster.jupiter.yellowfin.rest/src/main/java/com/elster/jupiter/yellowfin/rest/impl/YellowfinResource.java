package com.elster.jupiter.yellowfin.rest.impl;

import com.elster.jupiter.users.User;
import com.elster.jupiter.yellowfin.YellowfinService;
import com.elster.jupiter.yellowfin.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Path("/user")
public class YellowfinResource {

	private YellowfinService yellowfinService;



	@Inject
	private YellowfinResource(YellowfinService yellowfinService){
		this.yellowfinService = yellowfinService;
	}


	@POST
	@Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
	@Path("/login")
	@RolesAllowed({Privileges.Constants.VIEW_REPORTS,Privileges.Constants.DESIGN_REPORTS})
	public YellowfinInfo login(HttpServletResponse response, @Context SecurityContext securityContext) {
		User user = (User) securityContext.getUserPrincipal();

        yellowfinService.logout(user.getName()).
                orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("error.facts.unavailable").build()));
        String webServiceLoginToken = yellowfinService.login(user.getName()).
                orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("error.facts.unavailable").build()));

		YellowfinInfo info = new YellowfinInfo();
		info.token = webServiceLoginToken;
		info.url = yellowfinService.getYellowfinUrl();
		return info;
	}
	@POST
	@Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
	@Path("/token")
	@RolesAllowed(Privileges.Constants.VIEW_REPORTS)
	public YellowfinInfo token(HttpServletResponse response, @Context SecurityContext securityContext) {
		User user = (User) securityContext.getUserPrincipal();

		String webServiceLoginToken = yellowfinService.login(user.getName()).
                orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("error.facts.unavailable").build()));

		YellowfinInfo info = new YellowfinInfo();
		info.token = webServiceLoginToken;
		info.url = yellowfinService.getYellowfinUrl();
		return info;
	}

}
