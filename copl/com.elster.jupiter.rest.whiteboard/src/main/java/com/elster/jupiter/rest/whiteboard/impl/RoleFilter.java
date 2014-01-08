package com.elster.jupiter.rest.whiteboard.impl;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;

import com.elster.jupiter.security.thread.ThreadPrincipalService;

@Priority(Priorities.AUTHENTICATION)
public class RoleFilter implements ContainerRequestFilter {
	
	private final ThreadPrincipalService threadPrincipalService;
	
	RoleFilter(ThreadPrincipalService threadPrincipalService) {
		this.threadPrincipalService = threadPrincipalService;
	}

	@Override
	public void filter(ContainerRequestContext requestContext) {	
		requestContext.setSecurityContext(new SecurityContextImpl(requestContext,threadPrincipalService));
	}

}
