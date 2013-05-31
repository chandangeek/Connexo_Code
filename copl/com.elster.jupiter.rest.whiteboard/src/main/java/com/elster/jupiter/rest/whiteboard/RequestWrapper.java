package com.elster.jupiter.rest.whiteboard;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

class RequestWrapper extends HttpServletRequestWrapper {
	
	RequestWrapper(HttpServletRequest request) {
		super(request);
	}
	
	@Override
	public Principal getUserPrincipal() {
		return (Principal) getRequest().getAttribute(ServiceLocator.USERPRINCIPAL);
	}

}
