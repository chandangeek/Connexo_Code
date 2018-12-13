/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.insight.issue.datavalidation.rest.impl;

import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.rest.util.Transactional;

import com.elster.insight.issue.datavalidation.UsagePointIssueDataValidation;
import com.elster.insight.issue.datavalidation.UsagePointIssueDataValidationService;

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
public class UsagePointIssueResource {

    private final UsagePointIssueDataValidationService usagePointIssueDataValidationService;
    private final UsagePointDataValidationIssueInfoFactory issueInfoFactory;

    @Inject
    public UsagePointIssueResource(UsagePointIssueDataValidationService usagePointIssueDataValidationService, UsagePointDataValidationIssueInfoFactory dataCollectionIssuesInfoFactory) {
        this.usagePointIssueDataValidationService = usagePointIssueDataValidationService;
        this.issueInfoFactory = dataCollectionIssuesInfoFactory;
    }

    @GET @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    public Response getIssueById(@PathParam("id") long id) {
        UsagePointIssueDataValidation issue = usagePointIssueDataValidationService.findIssue(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return Response.ok(issueInfoFactory.asInfo(issue, DeviceInfo.class)).build();
    }
}