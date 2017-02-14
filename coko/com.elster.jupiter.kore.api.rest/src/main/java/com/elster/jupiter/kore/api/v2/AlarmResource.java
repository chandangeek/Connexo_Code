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
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;
import static java.util.stream.Collectors.toList;

@Path("/alarms")
public class AlarmResource {

    private final AlarmInfoFactory alarmInfoFactory;
    private final IssueStatusInfoFactory issueStatusInfoFactory;
    private final IssueCommentInfoFactory issueCommentInfoFactory;
    private final IssueService issueService;
    private final ExceptionFactory exceptionFactory;
    private final UserService userService;

    @Inject
    public AlarmResource(AlarmInfoFactory alarmInfoFactory, IssueStatusInfoFactory issueStatusInfoFactory, IssueCommentInfoFactory issueCommentInfoFactory, IssueService issueService, ExceptionFactory exceptionFactory, UserService userService) {
        this.alarmInfoFactory = alarmInfoFactory;
        this.issueStatusInfoFactory = issueStatusInfoFactory;
        this.issueCommentInfoFactory = issueCommentInfoFactory;
        this.issueService = issueService;
        this.exceptionFactory = exceptionFactory;
        this.userService = userService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public PagedInfoList<AlarmInfo> getAllAlarms(@BeanParam FieldSelection fieldSelection,
                                                 @Context UriInfo uriInfo,
                                                 @BeanParam JsonQueryParameters queryParameters) {
        //validateMandatory(params, START, LIMIT);
        IssueFilter filter = issueService.newIssueFilter();
        List<AlarmInfo> infos = issueService.findAlarms(filter).find().stream()
                .filter(alarm -> !alarm.getStatus().isHistorical())
                .map(isu -> alarmInfoFactory.from(isu, uriInfo, fieldSelection.getFields()))
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
    public IssueStatusInfo getStatus(@PathParam("id") long alarmId, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        IssueFilter filter = issueService.newIssueFilter();
        IssueStatus issueStatus = issueService.findAlarms(filter).find().stream().filter(alarm -> alarm.getId() == alarmId).findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_ALARM, String.valueOf(alarmId))).getStatus();
        return issueStatusInfoFactory.from(issueStatus, uriInfo, fieldSelection.getFields());
    }

    @PUT
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{id}/close")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public AlarmInfo closeIssue(@PathParam("id") long alarmId, IssueShortInfo alarmShortInfo, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        IssueFilter filter = issueService.newIssueFilter();
        Issue alarm = issueService.findAlarms(filter).find().stream().filter(alm -> alm.getId() == alarmId)
                .filter(alm -> !alm.getStatus().isHistorical()).findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_ALARM, String.valueOf(alarmId)));
        if (alarm.getStatus().isHistorical()) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.ALARM_ALREADY_CLOSED, "status.id");
        }
        if (alarmShortInfo == null || alarmShortInfo.version == null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.VERSION_MISSING, "version");
        }
        Issue baseIssue = issueService.findAndLockIssueByIdAndVersion(alarmId, alarmShortInfo.version)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.ALARM_LOCK_ATTEMPT_FAILED, alarmId));
        if (alarmShortInfo.status == null || alarmShortInfo.status.id == null || alarmShortInfo.status.id.isEmpty()) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.FIELD_MISSING, "status.id");
        }
        IssueStatus status = issueService.findStatus(alarmShortInfo.status.id)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_STATUS, alarmShortInfo.status.id));
        HistoricalIssue closedAlarm;
        if (status.isHistorical()) {
            closedAlarm = ((OpenIssue) baseIssue).close(status);
        } else {
            throw exceptionFactory.newException(MessageSeeds.BAD_FIELD_VALUE, "status.id");
        }
        return closedAlarm != null ? alarmInfoFactory.from(closedAlarm, uriInfo, fieldSelection.getFields()) : null;
    }


    @POST
    @Transactional
    @Path("/{id}/comment")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public Response addComment(@PathParam("id") long alarmId, IssueCommentInfo issueCommentInfo, @Context UriInfo uriInfo) {
        IssueFilter filter = issueService.newIssueFilter();
        Issue alarm = issueService.findAlarms(filter).find().stream().filter(alm -> alm.getId() == alarmId).findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_ALARM, String.valueOf(alarmId)));
        if (issueCommentInfo == null || issueCommentInfo.version == null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.BAD_FIELD_VALUE, "version");
        } else if (issueCommentInfo.author == null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.BAD_FIELD_VALUE, "author");
        } else if (issueCommentInfo.comment == null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.BAD_FIELD_VALUE, "comment");
        } else if (issueCommentInfo.creationDate == null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.BAD_FIELD_VALUE, "creationDate");
        }
        User user = userService.findUser(issueCommentInfo.author.name)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_USER, issueCommentInfo.author.id));
        alarm.addComment(issueCommentInfo.comment, user).orElseThrow(() -> new WebApplicationException(Response.Status.BAD_REQUEST));

        URI uri = uriInfo.getBaseUriBuilder().
                path(IssueResource.class).
                path(IssueResource.class, "getComments").
                build(alarm.getId());

        return Response.created(uri).build();
    }


    @GET
    @Transactional
    @Path("/{id}/comments")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public PagedInfoList<IssueCommentInfo> getComments(@PathParam("id") long alarmId,
                                                       @BeanParam FieldSelection fieldSelection,
                                                       @Context UriInfo uriInfo,
                                                       @BeanParam JsonQueryParameters queryParameters) {
        IssueFilter filter = issueService.newIssueFilter();
        Issue alarm = issueService.findAlarms(filter).find().stream().filter(alm -> alm.getId() == alarmId).findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_ALARM, String.valueOf(alarmId)));
        Condition condition = where("issueId").isEqualTo(alarm.getId());
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
        return alarmInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }
}
