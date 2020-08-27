/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.yellowfin.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.users.User;
import com.elster.jupiter.yellowfin.MessageSeeds;
import com.elster.jupiter.yellowfin.YellowfinService;
import com.elster.jupiter.yellowfin.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Path("/user")
public class YellowfinResource {
    public static final String USER_REPORTS_VIEWER = "reportsViewer";
    private final YellowfinService yellowfinService;
    private final Thesaurus thesaurus;


    @Inject
    private YellowfinResource(YellowfinService yellowfinService, Thesaurus thesaurus) {
        this.yellowfinService = yellowfinService;
        this.thesaurus = thesaurus;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/url")
    @RolesAllowed({Privileges.Constants.VIEW_REPORTS, Privileges.Constants.DESIGN_REPORTS})
    public YellowfinInfo getUrl() {
        YellowfinInfo info = new YellowfinInfo();
        info.token = "";
        info.url = yellowfinService.getYellowfinUrl();
        return info;
    }


    @POST
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/login")
    @RolesAllowed({Privileges.Constants.VIEW_REPORTS, Privileges.Constants.DESIGN_REPORTS})
    public YellowfinInfo login(HttpServletResponse response, @Context SecurityContext securityContext) {
        String userName = getName((User) securityContext.getUserPrincipal());
        String found = yellowfinService.getUser(userName).
                orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(thesaurus.getFormat(MessageSeeds.FACTS_NOT_AVAILABLE).format()).build()));

        if (found.equals("NOT_FOUND")) {
            found = yellowfinService.createUser(userName).
                    orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(thesaurus.getFormat(MessageSeeds.FACTS_NOT_AVAILABLE).format()).build()));
        } else if (found.equals("SUCCESS")) {
            yellowfinService.logout(userName).
                    orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(thesaurus.getFormat(MessageSeeds.FACTS_NOT_AVAILABLE).format()).build()));
        }

        if (found.equals("SUCCESS")) {
            String webServiceLoginToken = yellowfinService.login(userName).
                    orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(thesaurus.getFormat(MessageSeeds.FACTS_NOT_AVAILABLE).format()).build()));

            YellowfinInfo info = new YellowfinInfo();
            info.token = webServiceLoginToken;
            info.url = yellowfinService.getYellowfinUrl();
            return info;
        } else {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(thesaurus.getFormat(MessageSeeds.FACTS_NOT_AVAILABLE_ENHANCED).format(found)).build());
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/token")
    @RolesAllowed(Privileges.Constants.VIEW_REPORTS)
    public YellowfinInfo token(HttpServletResponse response, @Context SecurityContext securityContext) {
        String userName = getName((User) securityContext.getUserPrincipal());
        String found = yellowfinService.getUser(userName).
                orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(thesaurus.getFormat(MessageSeeds.FACTS_NOT_AVAILABLE).format()).build()));

        if (found.equals("NOT_FOUND")) {
            found = yellowfinService.createUser(userName).
                    orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(thesaurus.getFormat(MessageSeeds.FACTS_NOT_AVAILABLE).format()).build()));
        }

        if (found.equals("SUCCESS")) {
            String webServiceLoginToken = yellowfinService.login(userName).
                    orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(thesaurus.getFormat(MessageSeeds.FACTS_NOT_AVAILABLE).format()).build()));

            YellowfinInfo info = new YellowfinInfo();
            info.token = webServiceLoginToken;
            info.url = yellowfinService.getYellowfinUrl();
            return info;
        } else {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(thesaurus.getFormat(MessageSeeds.FACTS_NOT_AVAILABLE_ENHANCED).format(found)).build());
        }
    }

    private String getName(User user) {
        String userName = user.getName();
        if (! user.getPrivileges("YFN").stream().anyMatch(p -> p.getName().equals("privilege.design.reports") || p.getName().equals("privilege.administrate.reports"))) {
            userName = USER_REPORTS_VIEWER;
        }
        return userName;
    }
}
