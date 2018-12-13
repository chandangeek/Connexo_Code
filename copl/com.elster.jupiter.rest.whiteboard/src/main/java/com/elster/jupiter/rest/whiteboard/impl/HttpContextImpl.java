/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.whiteboard.impl;

import com.elster.jupiter.http.whiteboard.HttpAuthenticationService;

import org.osgi.service.http.HttpContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;

public class HttpContextImpl implements HttpContext {

    private final HttpAuthenticationService authorization;

    public HttpContextImpl(HttpAuthenticationService authorization) {
        this.authorization = authorization;
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

        return authorize;
    }
}
