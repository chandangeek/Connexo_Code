/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.whiteboard.impl;

import com.elster.jupiter.http.whiteboard.HttpAuthenticationService;
import com.elster.jupiter.users.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

class RequestWrapper extends HttpServletRequestWrapper {
	
	RequestWrapper(HttpServletRequest request) {
		super(request);
	}
	
	@Override
	public User getUserPrincipal() {
		return (User) getRequest().getAttribute(HttpAuthenticationService.USERPRINCIPAL);
	}

}
