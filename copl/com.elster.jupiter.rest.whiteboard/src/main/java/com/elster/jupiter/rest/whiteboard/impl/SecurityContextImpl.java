package com.elster.jupiter.rest.whiteboard.impl;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.User;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.SecurityContext;

import java.security.Principal;

public class SecurityContextImpl implements SecurityContext {
	
	private final SecurityContext oldContext;
	private final ThreadPrincipalService threadPrincipalService;
	private final String applicationName;
	
	SecurityContextImpl(ContainerRequestContext request, ThreadPrincipalService threadPrincipalService) {
		this.oldContext = request.getSecurityContext();
		this.applicationName = request.getHeaders().getFirst("X-CONNEXO-APPLICATION-NAME");
		this.threadPrincipalService = threadPrincipalService;
	}
	
	@Override
	public String getAuthenticationScheme() {
		return oldContext.getAuthenticationScheme();
	}

	@Override
	public Principal getUserPrincipal() {
		return threadPrincipalService.getPrincipal();
	}

	@Override
	public boolean isSecure() {
		return oldContext.isSecure();
	}

	@Override
	public boolean isUserInRole(String role) {		
		User user = (User) getUserPrincipal();
		return user == null ? false : user.hasPrivilege(applicationName, role);
	}

}
