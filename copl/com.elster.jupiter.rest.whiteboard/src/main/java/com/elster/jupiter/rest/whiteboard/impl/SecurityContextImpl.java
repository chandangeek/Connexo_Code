package com.elster.jupiter.rest.whiteboard.impl;

import com.elster.jupiter.users.User;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

public class SecurityContextImpl implements SecurityContext {
	
	private final SecurityContext oldContext;
	
	SecurityContextImpl(ContainerRequestContext request) {
		this.oldContext = request.getSecurityContext();
	}
	
	@Override
	public String getAuthenticationScheme() {
		return oldContext.getAuthenticationScheme();
	}

	@Override
	public Principal getUserPrincipal() {
		return Bus.getThreadPrincipalService().getPrincipal();
	}

	@Override
	public boolean isSecure() {
		return oldContext.isSecure();
	}

	@Override
	public boolean isUserInRole(String role) {		
		User user = (User) getUserPrincipal();
		System.out.println("Checking role " + role);		
		return user == null ? false : user.hasPrivilege(role);
	}

}
