/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue.rest.impl;

import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.webservice.issue.WebServiceIssue;
import com.elster.jupiter.webservice.issue.WebServiceIssueService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/issues")
public class IssueResource {
    private final WebServiceIssueService webServiceIssueService;
    private final WebServiceIssueInfoFactory issueInfoFactory;

    @Inject
    public IssueResource(WebServiceIssueService webServiceIssueService, WebServiceIssueInfoFactory webServiceIssueInfoFactory) {
        this.webServiceIssueService = webServiceIssueService;
        this.issueInfoFactory = webServiceIssueInfoFactory;
    }

    @GET
    @Path("/{id}")
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    public Response getIssueById(@PathParam("id") long id) {
        WebServiceIssue issue = webServiceIssueService.findIssue(id)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return Response.ok(issueInfoFactory.from(issue)).build();
    }
}
