package com.elster.jupiter.yellowfin.rest.impl;

import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.yellowfin.YellowfinService;

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
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/login")

	public YellowfinInfo login(HttpServletResponse response, @Context SecurityContext securityContext) {
		User user = (User) securityContext.getUserPrincipal();

		yellowfinService.logout(user.getName());
		String webServiceLoginToken = yellowfinService.login(user.getName());

		YellowfinInfo info = new YellowfinInfo();
		if(webServiceLoginToken!=null) {
			info.token = webServiceLoginToken;
			info.url = yellowfinService.getYellowfinUrl();
		}
		else{
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
		return info;
	}

}
