package com.elster.jupiter.rest.whiteboard.impl;

import com.elster.jupiter.users.User;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

public class SecurityContextImpl implements SecurityContext {
	private final HttpServletRequest request;
	
	SecurityContextImpl(HttpServletRequest request) {
		this.request = request;
	}
	
	@Override
	public String getAuthenticationScheme() {
		switch (request.getAuthType()) {
			case HttpServletRequest.BASIC_AUTH:
				return SecurityContext.BASIC_AUTH;
			case HttpServletRequest.CLIENT_CERT_AUTH:
				return SecurityContext.CLIENT_CERT_AUTH;
			case HttpServletRequest.DIGEST_AUTH:
				return SecurityContext.DIGEST_AUTH;
			case HttpServletRequest.FORM_AUTH:
				return SecurityContext.FORM_AUTH;
			default:
				return null;
					
		}
	}

	@Override
	public Principal getUserPrincipal() {
		// attribute is set by HttpContextImpl
		return (Principal) request.getAttribute(ServiceLocator.USERPRINCIPAL);
	}

	@Override
	public boolean isSecure() {
		return request.isSecure();
	}

	@Override
	public boolean isUserInRole(String role) {		
		User user = (User) getUserPrincipal();
		return user == null ? false : user.hasPrivilege(role);
	}

}
