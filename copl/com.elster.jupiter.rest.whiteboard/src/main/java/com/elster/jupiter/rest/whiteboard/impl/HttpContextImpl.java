package com.elster.jupiter.rest.whiteboard.impl;

import org.osgi.service.http.HttpContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;

public class HttpContextImpl implements HttpContext {
	
	private final Authentication authorization;
    private final WhiteBoard whiteboard;

    // Inactivity interval in seconds - session will expire after this
    static final int INACTIVE_INTERVAL = 600;
	
	public HttpContextImpl(WhiteBoard whiteboard, Authentication authorization) {
		this.authorization = authorization;
        this.whiteboard = whiteboard;
	}

    @Override
    public String getMimeType(String name) {
        return null;
    }

    @Override
    public URL getResource(String name) {
        return getClass().getResource(name);
    }

    @Override
    public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
        boolean authorize = authorization.handleSecurity(request,response);
        if(!authorize && request.getHeader("referer") != null){
            response.setHeader("WWW-Authenticate","Custom");
        }

        request.getSession(true).setMaxInactiveInterval(whiteboard.getSessionTimeout());
        return authorize;
    }
}
