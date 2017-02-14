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
import java.util.Arrays;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;
import static java.util.stream.Collectors.toList;

@Path("/issues")
public class IssueResource {

    private final IssueInfoFactory issueInfoFactory;
    private final IssueStatusInfoFactory issueStatusInfoFactory;
    private final IssueCommentInfoFactory issueCommentInfoFactory;
    private final IssueService issueService;
    private final ExceptionFactory exceptionFactory;
    private final UserService userService;

    @Inject
    public IssueResource(IssueInfoFactory issueInfoFactory, IssueStatusInfoFactory issueStatusInfoFactory, IssueCommentInfoFactory issueCommentInfoFactory, IssueService issueService, ExceptionFactory exceptionFactory, UserService userService) {
        this.issueInfoFactory = issueInfoFactory;
        this.issueStatusInfoFactory = issueStatusInfoFactory;
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
        List<IssueInfo> infos = issueService.findIssues(filter).find().stream()
                .filter(isu -> !isu.getStatus().isHistorical())
                .map(isu -> issueInfoFactory.from(isu, uriInfo, fieldSelection.getFields()))
                .collect(toList());
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(IssueResource.class);
        return PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo);
        //addSorting(finder, params);
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{id}/status")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public IssueStatusInfo getStatus(@PathParam("id") long issueId, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        IssueStatus issueStatus = issueService.findIssue(issueId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_ISSUE, String.valueOf(issueId))).getStatus();
        return issueStatusInfoFactory.from(issueStatus, uriInfo, fieldSelection.getFields());
    }

    @PUT
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{id}/close")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public IssueInfo closeIssue(@PathParam("id") long issueId, IssueShortInfo issueShortInfo, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        if (issueShortInfo == null || issueShortInfo.version == null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.VERSION_MISSING, "version");
        }
        Issue issue = issueService.findAndLockIssueByIdAndVersion(issueId, issueShortInfo.version).filter(isu -> !isu.getStatus().isHistorical())
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_ISSUE, String.valueOf(issueId)));
        if (issueShortInfo.status == null || issueShortInfo.status.id == null || issueShortInfo.status.id.isEmpty()) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.FIELD_MISSING, "status.id");
        }
        IssueStatus status = issueService.findStatus(issueShortInfo.status.id)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_STATUS, issueShortInfo.status.id));
        HistoricalIssue closedIssue = null;
        if (status.isHistorical()) {
            closedIssue = ((OpenIssue) issue).close(status);
        } else {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.BAD_FIELD_VALUE, "status.id");
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
        Issue issue = issueService.findIssue(issueId).orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_ISSUE, String.valueOf(issueId)));
        User user = userService.findUser(issueCommentInfo.author.name)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_USER, issueCommentInfo.author.id));
        issue.addComment(issueCommentInfo.comment, user).orElseThrow(() -> new WebApplicationException(Response.Status.BAD_REQUEST));
        if (issueCommentInfo == null || issueCommentInfo.version == null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.BAD_FIELD_VALUE, "version");
        } else if (issueCommentInfo.author == null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.BAD_FIELD_VALUE, "author");
        } else if (issueCommentInfo.comment == null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.BAD_FIELD_VALUE, "comment");
        } else if (issueCommentInfo.creationDate == null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.BAD_FIELD_VALUE, "creationDate");
        }
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
        Issue issue = issueService.findIssue(issueId).orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_ISSUE, String.valueOf(issueId)));
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
