/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.IssueFilter;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueComment;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.kore.api.impl.MessageSeeds;
import com.elster.jupiter.kore.api.security.Privileges;
import com.elster.jupiter.rest.api.util.v1.hypermedia.FieldSelection;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PagedInfoList;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.util.conditions.Where.where;
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
    private final IssueCommentInfoFactory issueCommentInfoFactory;
    private final IssueService issueService;
    private final ExceptionFactory exceptionFactory;
    private final UserService userService;

    @Inject
    public IssueResource(IssueInfoFactory issueInfoFactory, EndDeviceInfoFactory endDeviceInfoFactory, IssueStatusInfoFactory issueStatusInfoFactory, IssueAssigneeInfoFactory issueAssigneeInfoFactory, IssueTypeInfoFactory issueTypeInfoFactory, IssuePriorityInfoFactory issuePriorityInfoFactory, IssueReasonInfoFactory issueReasonInfoFactory, IssueCommentInfoFactory issueCommentInfoFactory, IssueService issueService, ExceptionFactory exceptionFactory, UserService userService) {
        this.issueInfoFactory = issueInfoFactory;
        this.endDeviceInfoFactory = endDeviceInfoFactory;
        this.issueStatusInfoFactory = issueStatusInfoFactory;
        this.issueAssigneeInfoFactory = issueAssigneeInfoFactory;
        this.issueTypeInfoFactory = issueTypeInfoFactory;
        this.issuePriorityInfoFactory = issuePriorityInfoFactory;
        this.issueReasonInfoFactory = issueReasonInfoFactory;
        this.issueCommentInfoFactory = issueCommentInfoFactory;
        this.issueService = issueService;
        this.exceptionFactory = exceptionFactory;
        this.userService = userService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public PagedInfoList<IssueInfo> getAllIssues(@BeanParam FieldSelection fieldSelection,
                                                 @Context UriInfo uriInfo,
                                                 @BeanParam JsonQueryParameters queryParameters) {
        //validateMandatory(params, START, LIMIT);
        IssueFilter filter = issueService.newIssueFilter();
        Arrays.asList("datacollection", "datavalidation").stream().forEach(listItem -> issueService.findIssueType(listItem).ifPresent(filter::addIssueType));
        Arrays.asList(IssueStatus.OPEN, IssueStatus.IN_PROGRESS).stream().forEach(listItem -> issueService.findStatus(listItem).ifPresent(filter::addStatus));
        List<IssueInfo> infos = issueService.findIssues(filter).find().stream()
                .map(isu -> issueInfoFactory.from(isu, uriInfo, fieldSelection.getFields()))
                .collect(toList());
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(IssueResource.class);
        return PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo);
        //addSorting(finder, params);

    }


    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/status/{id}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public IssueStatusInfo getEndDevice(@PathParam("id") long issueId, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        IssueStatus issueStatus = issueService.findIssue(issueId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_ISSUE, issueId)).getStatus();
        return issueStatusInfoFactory.from(issueStatus, uriInfo, fieldSelection.getFields());
    }

    @PUT
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{id}/close")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public IssueInfo closeIssue(@PathParam("id") long issueId, IssueStatusInfo issueStatusInfo, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        Issue issue = issueService.findIssue(issueId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_ISSUE, issueId));
        //and lock by version?
        Optional<IssueStatus> status = issueService.findStatus(IssueStatus.RESOLVED);
        HistoricalIssue closedIssue = null;
        if (!issue.getStatus().isHistorical() && status.isPresent()) {
            closedIssue = ((OpenIssue) issue).close(status.get());
        }
        return closedIssue != null ? issueInfoFactory.from(closedIssue, uriInfo, fieldSelection.getFields()) : null;
    }


    @POST
    @Transactional
    @Path("/{id}/comment")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public Response addComment(@PathParam("id") long issueId, IssueCommentInfo issueCommentInfo, @Context UriInfo uriInfo) {
        Issue issue = issueService.findIssue(issueId).orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_ISSUE, issueId));
        User user = userService.findUser(issueCommentInfo.author.name).orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_USER, issueCommentInfo.author.id));
        issue.addComment(issueCommentInfo.comment, user).orElseThrow(() -> new WebApplicationException(Response.Status.BAD_REQUEST));

        URI uri = uriInfo.getBaseUriBuilder().
                path(IssueResource.class).
                path(IssueResource.class, "getComments").
                build(issue.getId());

        return Response.created(uri).build();
    }


    @GET
    @Transactional
    @Path("/{id}/comments")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public PagedInfoList<IssueCommentInfo> getComments(@PathParam("id") long issueId,
                                                       @BeanParam FieldSelection fieldSelection,
                                                       @Context UriInfo uriInfo,
                                                       @BeanParam JsonQueryParameters queryParameters) {
        Issue issue = issueService.findIssue(issueId).orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_ISSUE, issueId));
        Condition condition = where("issueId").isEqualTo(issue.getId());
        Query<IssueComment> query = issueService.query(IssueComment.class, User.class);
        List<IssueComment> commentsList = query.select(condition, Order.ascending("createTime"));
        List<IssueCommentInfo> infos = commentsList.stream().map(isu -> issueCommentInfoFactory.from(isu, uriInfo, fieldSelection.getFields()))
                .collect(toList());
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(IssueResource.class);
        return PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo);

    }


    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public List<String> getFields() {
        return issueInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }
}
