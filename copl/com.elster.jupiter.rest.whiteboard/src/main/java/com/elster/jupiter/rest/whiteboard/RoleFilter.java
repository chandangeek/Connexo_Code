package com.elster.jupiter.rest.whiteboard;

import javax.servlet.http.HttpServletRequest;
import com.sun.jersey.spi.container.*;

public class RoleFilter implements ContainerRequestFilter {

	@javax.ws.rs.core.Context
	HttpServletRequest httpServletRequest;
	
	@Override
	public ContainerRequest filter(ContainerRequest request) {		
		request.setSecurityContext(new SecurityContextImpl(httpServletRequest));
		return request;
	}

}
