/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl.rest;

import com.elster.jupiter.http.whiteboard.HttpAuthenticationService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.User;
import org.apache.cxf.BusFactory;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ServletWrapper extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private final HttpServlet servlet;
	private final ThreadPrincipalService threadPrincipalService;

	public ServletWrapper(HttpServlet servlet, ThreadPrincipalService threadPrincipalService) {
		this.servlet = servlet;
		this.threadPrincipalService = threadPrincipalService;
	}
	
	@Override
	public void init() throws ServletException {
		super.init();
		servlet.init(getServletConfig());
	}
	
	public void destroy() {
		servlet.destroy();
		super.destroy();		
	}
	
	protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		threadPrincipalService.set(getPrincipal(request));
		try {
			servlet.service(request,response);
		}finally {
			threadPrincipalService.clear();
		}
	}

	private User getPrincipal(final HttpServletRequest request){
		return (User) request.getAttribute(HttpAuthenticationService.USERPRINCIPAL);
	}

}
