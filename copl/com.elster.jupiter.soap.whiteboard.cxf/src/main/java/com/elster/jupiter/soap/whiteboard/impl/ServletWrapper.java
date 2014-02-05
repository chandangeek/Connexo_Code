package com.elster.jupiter.soap.whiteboard.impl;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cxf.BusFactory;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;

public class ServletWrapper extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final CXFNonSpringServlet servlet;
	
	ServletWrapper(CXFNonSpringServlet servlet) {
		this.servlet = servlet;
	}
	
	@Override
	public void init() throws ServletException {
		super.init();
		servlet.init(getServletConfig());
		BusFactory.setDefaultBus(servlet.getBus());
	}
	
	public void destroy() {
		servlet.destroy();
		super.destroy();		
	}
	
	protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		servlet.service(request,response);
	}
	
}
