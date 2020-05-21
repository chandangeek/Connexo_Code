/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.whiteboard.impl;

import com.elster.jupiter.http.whiteboard.CSRFFilterService;
import com.elster.jupiter.http.whiteboard.HttpAuthenticationService;
import com.elster.jupiter.rest.util.MimeTypesExt;

import org.osgi.service.http.HttpContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;

public class HttpContextImpl implements HttpContext {

    private final HttpAuthenticationService authorization;
    private final CSRFFilterService csrfFilterService;

    public HttpContextImpl(HttpAuthenticationService authorization, CSRFFilterService csrfFilterService) {
        this.authorization = authorization;
        this.csrfFilterService = csrfFilterService;
	}

    @Override
    public String getMimeType(String name) {
        return MimeTypesExt.get().getByFile(name);
    }

    @Override
    public URL getResource(String name) {
        return getClass().getResource(name);
    }

    @Override
    public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
        boolean authorize = csrfFilterService.handleCSRFSecurity(request, response);
        if(authorize) {
             authorize = authorization.handleSecurity(request, response);
        }
        if(!authorize && request.getHeader("referer") != null){
            response.setHeader("WWW-Authenticate","Custom");
        }
        response.addHeader("X-Content-Type-Options", "nosniff");
        response.setContentType("application/octet-stream");
        return authorize;
    }
}
