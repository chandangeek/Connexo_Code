/*
 *
 *  * Copyright (c) 2020  by Honeywell International Inc. All Rights Reserved
 *
 *
 */

package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.users.CSRFService;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.Optional;


/**
 * Insert your comments here.
 *
 * @author E492165 (M R)
 * @since 1/30/2020 (09:22)
 */
@Path("/csrf")
public class CSRFContollerService {
    private final String USER_SESSIONID = "X-SESSIONID";
    private CSRFService csrfService;

    @Inject
    public CSRFContollerService(CSRFService csrfService){
        this.csrfService = csrfService;
    }

    @GET
    @Path("/token")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public String getToken(HttpServletRequest request) {
        Optional<Cookie> sessionId = getSessionCookie(request);
        String csrfToken = csrfService.getCSRFToken(sessionId.get().getValue());
        if(null != csrfToken) {
            return csrfToken;
        }
        return "InvalidToken";
    }

    private Optional<Cookie> getSessionCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(cookie -> USER_SESSIONID.equals(cookie.getName()))
                    .findFirst();
        }
        return Optional.empty();
    }
}
