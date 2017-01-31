/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.http.whiteboard.HttpAuthenticationService;
import com.elster.jupiter.http.whiteboard.Resolver;

import com.google.common.collect.ImmutableMap;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;


public class HttpContextImpl implements HttpContext {

    private final Resolver resolver;
    private final AtomicReference<EventAdmin> eventAdminHolder;
    private final HttpAuthenticationService authenticationService;

    HttpContextImpl(Resolver resolver, AtomicReference<EventAdmin> eventAdminHolder, HttpAuthenticationService authenticationService) {
        this.resolver = resolver;
        this.eventAdminHolder = eventAdminHolder;
        this.authenticationService = authenticationService;
    }

    @Override
    public String getMimeType(String arg0) {
        return null;
    }

    @Override
    public URL getResource(String name) {
        return resolver.getResource(name);
    }

    @Override
    public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
        fireHttpEvent(request);
        return authenticationService.handleSecurity(request, response);
    }

    private void fireHttpEvent(HttpServletRequest request) {
        EventAdmin eventAdmin = eventAdminHolder.get();
        if (eventAdmin != null) {
            StringBuffer requestUrl = request.getRequestURL();
            String queryString = request.getQueryString();
            if (queryString != null) {
                requestUrl.append("?").append(queryString);
            }
            Event event = new Event("com/elster/jupiter/http/GET", ImmutableMap.of("resource", requestUrl.toString()));
            eventAdmin.postEvent(event);
        }
    }
}
