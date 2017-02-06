/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.issue.share.IssueFilter;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.kore.api.security.Privileges;
import com.elster.jupiter.rest.api.util.v1.hypermedia.FieldSelection;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PagedInfoList;


import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Path("/issues")
public class IssueResource {

    private final IssueInfoFactory issueInfoFactory;
    private final EndDeviceInfoFactory endDeviceInfoFactory;
    private final IssueStatusInfoFactory issueStatusInfoFactory;
    private final IssueAssigneeInfoFactory issueAssigneeInfoFactory;
    private final IssueTypeInfoFactory issueTypeInfoFactory;
    private final IssuePriorityInfoFactory issuePriorityInfoFactory;
    private final IssueReasonInfoFactory issueReasonInfoFactory;
    private final IssueService issueService;

    @Inject
    public IssueResource(IssueInfoFactory issueInfoFactory, EndDeviceInfoFactory endDeviceInfoFactory, IssueStatusInfoFactory issueStatusInfoFactory, IssueAssigneeInfoFactory issueAssigneeInfoFactory, IssueTypeInfoFactory issueTypeInfoFactory, IssuePriorityInfoFactory issuePriorityInfoFactory, IssueReasonInfoFactory issueReasonInfoFactory, IssueService issueService) {
        this.issueInfoFactory = issueInfoFactory;
        this.endDeviceInfoFactory = endDeviceInfoFactory;
        this.issueStatusInfoFactory = issueStatusInfoFactory;
        this.issueAssigneeInfoFactory = issueAssigneeInfoFactory;
        this.issueTypeInfoFactory = issueTypeInfoFactory;
        this.issuePriorityInfoFactory = issuePriorityInfoFactory;
        this.issueReasonInfoFactory = issueReasonInfoFactory;
        this.issueService = issueService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public PagedInfoList<IssueInfo> getAllIssues(@BeanParam FieldSelection fieldSelection,
                                                 @Context UriInfo uriInfo,
                                                 @BeanParam JsonQueryParameters queryParameters) {
        //validateMandatory(params, START, LIMIT);

       List<IssueType> issueTypeList = new ArrayList<>();
        IssueFilter filter = issueService.newIssueFilter();
        new ArrayList<>(Arrays.asList("datacollection", "datavalidation")).stream().forEach(listItem -> issueService.findIssueType(listItem).ifPresent(filter::addIssueType));
        List<IssueInfo> infos = issueService.findIssues(filter).find().stream()
                //.orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.BAD_REQUEST, MessageSeeds.NO_SUCH_ISSUE))
                .map(isu -> issueInfoFactory.from(isu, uriInfo, fieldSelection.getFields()))
                .collect(toList());
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(IssueResource.class);
        return PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo);
        //addSorting(finder, params);

    }
}
