package com.elster.jupiter.rest.whiteboard.impl;

import com.elster.jupiter.security.thread.ThreadPrincipalService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class ServletWrapper extends HttpServlet  {
	private static final long serialVersionUID = 1L;
	private final HttpServlet servlet;
	
	public ServletWrapper(HttpServlet servlet) {
		this.servlet = servlet;
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
		request = new RequestWrapper(request);
		ThreadPrincipalService service = Bus.getThreadPrincipalService();
		service.set(request.getUserPrincipal());
		try {
			servlet.service(request,response);
		} finally {
			service.clear();
		}
	}

}
