package com.elster.jupiter.rest.whiteboard.impl;

import javax.annotation.Priority;
import javax.ws.rs.container.*;
import javax.ws.rs.*;

@Priority(Priorities.AUTHENTICATION)
public class RoleFilter implements ContainerRequestFilter {

	@Override
	public void filter(ContainerRequestContext requestContext) {	
		requestContext.setSecurityContext(new SecurityContextImpl(requestContext));
	}

}
