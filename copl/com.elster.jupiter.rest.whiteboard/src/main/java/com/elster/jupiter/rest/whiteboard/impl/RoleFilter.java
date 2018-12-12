/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.whiteboard.impl;

import com.elster.jupiter.security.thread.ThreadPrincipalService;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;

@Priority(Priorities.AUTHENTICATION)
public class RoleFilter implements ContainerRequestFilter {
	
	private final ThreadPrincipalService threadPrincipalService;

	@Inject
	public RoleFilter(ThreadPrincipalService threadPrincipalService) {
		this.threadPrincipalService = threadPrincipalService;
	}

	@Override
	public void filter(ContainerRequestContext requestContext) {	
		requestContext.setSecurityContext(new SecurityContextImpl(requestContext,threadPrincipalService));
	}

}
