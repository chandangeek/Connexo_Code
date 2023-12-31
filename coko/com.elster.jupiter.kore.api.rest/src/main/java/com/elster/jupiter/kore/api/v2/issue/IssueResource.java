/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2.issue;

import com.elster.jupiter.issue.share.IssueFilter;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.Issue;
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
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.User;

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
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Path("/issues")
public class IssueResource {

    private final IssueInfoFactory issueInfoFactory;
    private final IssueShortInfoFactory issueShortInfoFactory;
    private final IssueService issueService;
    private final ExceptionFactory exceptionFactory;
    private final ThreadPrincipalService threadPrincipalService;

    @Inject
    public IssueResource(IssueInfoFactory issueInfoFactory, IssueShortInfoFactory issueShortInfoFactory, IssueService issueService, ExceptionFactory exceptionFactory,
                         ThreadPrincipalService threadPrincipalService) {
        this.issueInfoFactory = issueInfoFactory;
        this.issueShortInfoFactory = issueShortInfoFactory;
        this.issueService = issueService;
        this.exceptionFactory = exceptionFactory;
        this.threadPrincipalService = threadPrincipalService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public PagedInfoList<IssueInfo> getAllOpenIssues(@BeanParam FieldSelection fieldSelection,
                                                 @Context UriInfo uriInfo,
                                                 @BeanParam JsonQueryParameters queryParameters) {
        IssueFilter filter = issueService.newIssueFilter();
        Arrays.asList("datacollection", "datavalidation").stream().forEach(listItem -> issueService.findIssueType(listItem).ifPresent(filter::addIssueType));
        List<IssueInfo> infos = issueService.findIssues(filter).from(queryParameters).stream()
                .filter(isu -> !isu.getStatus().isHistorical())
                .map(isu -> issueInfoFactory.from(isu, uriInfo, fieldSelection.getFields()))
                .collect(toList());
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(IssueResource.class);
        return PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo);
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{id}/status")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public IssueShortInfo getStatus(@PathParam("id") long issueId) {
        return issueShortInfoFactory.asInfo(issueService.findIssue(issueId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_ISSUE, String.valueOf(issueId))));
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
        Optional<? extends Issue> issue = issueService.findIssue(issueId);
        if (!issue.isPresent()) {
            throw exceptionFactory.newException(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_ISSUE, String.valueOf(issueId));
        } else if (issue.get().getStatus().isHistorical()) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.ISSUE_ALREADY_CLOSED, String.valueOf(issueId));
        }
        Issue lockedIssue = issueService.findAndLockIssueByIdAndVersion(issueId, issueShortInfo.version)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.ISSUE_LOCK_ATTEMPT_FAILED, String.valueOf(issueId)));
        if (lockedIssue.getStatus().isHistorical()) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.ISSUE_ALREADY_CLOSED, String.valueOf(issueId));
        }
        if (issueShortInfo.status == null || issueShortInfo.status.id == null || issueShortInfo.status.id.isEmpty()) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.FIELD_MISSING, "status.id");
        }
        IssueStatus status = issueService.findStatus(issueShortInfo.status.id)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_STATUS, issueShortInfo.status.id));
        HistoricalIssue closedIssue;
        if (status.isHistorical()) {
            closedIssue = ((OpenIssue) lockedIssue).close(status);
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
    public Response addComment(@PathParam("id") long issueId, IssueCommentShortInfo issueCommentShortInfo, @Context UriInfo uriInfo) {
        Issue issue = issueService.findIssue(issueId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_ISSUE, String.valueOf(issueId)));
        if (issueCommentShortInfo == null || issueCommentShortInfo.comment == null || issueCommentShortInfo.comment.isEmpty()) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.BAD_FIELD_VALUE, "comment");
        }
        issue.addComment(issueCommentShortInfo.comment, getCurrentUser()).orElseThrow(() -> new WebApplicationException(Response.Status.BAD_REQUEST));
        URI uri = uriInfo.getBaseUriBuilder().
                path(IssueResource.class).
                build();
        return Response.created(uri).build();
    }

    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public List<String> getFields() {
        return issueInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }

    private User getCurrentUser() {
        Principal currentUser = threadPrincipalService.getPrincipal();
        if (currentUser instanceof User) {
            return (User) currentUser;
        } else {
            throw exceptionFactory.newException(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_USER, currentUser.getName());
        }
    }


}

