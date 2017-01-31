/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.whiteboard.impl;

import com.elster.jupiter.security.thread.ThreadPrincipalService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;


public class ServletWrapper extends HttpServlet  {
	private static final long serialVersionUID = 1L;
	private final HttpServlet servlet;
	private final ThreadPrincipalService threadPrincipalService;
	private static final String APPLICATION_NAME_HEADER = "X-CONNEXO-APPLICATION-NAME";
	
	public ServletWrapper(HttpServlet servlet,ThreadPrincipalService threadPrincipalService) {
		this.servlet = servlet;
		this.threadPrincipalService = threadPrincipalService;
	}

	public void destroy() {
		servlet.destroy();
		super.destroy();
	}
	
	@Override
	public void init() throws ServletException {
		super.init();
		servlet.init(getServletConfig());
	}
	
	protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        doService(new RequestWrapper(request), response);
	}

    private void doService(RequestWrapper request, HttpServletResponse response) throws ServletException, IOException {
        threadPrincipalService.set(request.getUserPrincipal(), getModule(request), request.getMethod(), determineLocale(request));
		threadPrincipalService.setApplicationName(request.getHeader(APPLICATION_NAME_HEADER));
        try {
            servlet.service(request,response);
        } finally {
            threadPrincipalService.clear();
        }
    }

    private Locale determineLocale(RequestWrapper request) {
		// Locale of the authenticated user
		if (request.getUserPrincipal() != null) {
			return request.getUserPrincipal().getLocale().orElse(request.getLocale());
		}

		return request.getLocale();
	}

    // returns the first two parts
	private String getModule(HttpServletRequest request) {
		String result = request.getServletPath();
		String path = request.getPathInfo();
		if (path == null) {
			return result;
		} else {
			String[] parts = request.getPathInfo().split("/",3);
			if (parts.length == 0) {
				return result;
			} else {
				return result + "/" + parts[1];
			}
		}				
	}

}
