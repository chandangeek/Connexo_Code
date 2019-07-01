/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.servicecall.rest.impl;

import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.servicecall.issue.ServiceCallIssue;
import com.elster.jupiter.servicecall.issue.ServiceCallIssueService;

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

    private final ServiceCallIssueService serviceCallIssueService;
    private final com.elster.jupiter.issue.servicecall.rest.impl.ServiceCallIssueInfoFactory serviceCallIssueInfoFactory;

    @Inject
    public IssueResource(ServiceCallIssueService serviceCallIssueService, com.elster.jupiter.issue.servicecall.rest.impl.ServiceCallIssueInfoFactory serviceCallIssueInfoFactory) {
        this.serviceCallIssueService = serviceCallIssueService;
        this.serviceCallIssueInfoFactory = serviceCallIssueInfoFactory;
    }

    @GET @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    public Response getIssueById(@PathParam("id") long id) {
        ServiceCallIssue issue = serviceCallIssueService.findIssue(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return Response.ok(serviceCallIssueInfoFactory.asInfo(issue, DeviceInfo.class)).build();
    }
}