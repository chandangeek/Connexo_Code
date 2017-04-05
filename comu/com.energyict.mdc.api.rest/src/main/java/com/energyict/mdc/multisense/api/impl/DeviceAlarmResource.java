/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.kore.api.v2.issue.IssueCommentShortInfo;
import com.elster.jupiter.kore.api.v2.issue.IssueShortInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.FieldSelection;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PagedInfoList;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.User;
import com.energyict.mdc.device.alarms.DeviceAlarmFilter;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.energyict.mdc.device.alarms.entity.HistoricalDeviceAlarm;
import com.energyict.mdc.device.alarms.entity.OpenDeviceAlarm;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.security.Privileges;

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
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Path("/alarms")
public class DeviceAlarmResource {

    private final DeviceAlarmInfoFactory deviceAlarmInfoFactory;
    private final DeviceAlarmShortInfoFactory deviceAlarmShortInfoFactory;
    private final IssueService issueService;
    private final DeviceAlarmService deviceAlarmService;
    private final ExceptionFactory exceptionFactory;
    private final ThreadPrincipalService threadPrincipalService;

    @Inject
    public DeviceAlarmResource(DeviceAlarmInfoFactory deviceAlarmInfoFactory, DeviceAlarmShortInfoFactory deviceAlarmShortInfoFactory, IssueService issueService, DeviceAlarmService deviceAlarmService, ExceptionFactory exceptionFactory, ThreadPrincipalService threadPrincipalService) {
        this.deviceAlarmInfoFactory = deviceAlarmInfoFactory;
        this.deviceAlarmShortInfoFactory = deviceAlarmShortInfoFactory;
        this.issueService = issueService;
        this.deviceAlarmService = deviceAlarmService;
        this.exceptionFactory = exceptionFactory;
        this.threadPrincipalService = threadPrincipalService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public PagedInfoList<DeviceAlarmInfo> getAllOpenAlarms(@BeanParam FieldSelection fieldSelection,
                                                           @Context UriInfo uriInfo,
                                                           @BeanParam JsonQueryParameters queryParameters) {
        //validateMandatory(params, START, LIMIT);
        List<DeviceAlarmInfo> infos = deviceAlarmService.findAlarms(new DeviceAlarmFilter())
                .from(queryParameters).stream()
                .filter(alm -> !alm.getStatus().isHistorical())
                .map(isu -> deviceAlarmInfoFactory.from(isu, uriInfo, fieldSelection.getFields()))
                .collect(toList());
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(DeviceAlarmResource.class);
        return PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo);
        //addSorting(finder, params);

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{id}/status")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public DeviceAlarmShortInfo getStatus(@PathParam("id") long alarmId, @BeanParam FieldSelection fieldSelection) {
        DeviceAlarm alarm = deviceAlarmService.findAlarm(alarmId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_ALARM, String.valueOf(alarmId)));
        return deviceAlarmShortInfoFactory.asInfo(alarm);
    }

    @PUT
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{id}/close")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public DeviceAlarmInfo closeAlarm(@PathParam("id") long alarmId, DeviceAlarmShortInfo alarmShortInfo, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        Optional<? extends DeviceAlarm> alarm = deviceAlarmService.findAlarm(alarmId);
        if (!alarm.isPresent()) {
            throw exceptionFactory.newException(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_ALARM, String.valueOf(alarmId));
        } else if (alarm.get().getStatus().isHistorical()) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.ALARM_ALREADY_CLOSED, String.valueOf(alarmId));
        }

        if (alarmShortInfo == null || alarmShortInfo.version == null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.VERSION_MISSING, "version");
        }
        DeviceAlarm lockedAlarm = deviceAlarmService.findAndLockDeviceAlarmByIdAndVersion(alarmId, alarmShortInfo.version)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.ALARM_LOCK_ATTEMPT_FAILED, String.valueOf(alarmId)));
        if (alarmShortInfo.status == null || alarmShortInfo.status.id == null || alarmShortInfo.status.id.isEmpty()) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.FIELD_MISSING, "status.id");
        }
        IssueStatus status = issueService.findStatus(alarmShortInfo.status.id)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_STATUS, alarmShortInfo.status.id));
        HistoricalDeviceAlarm closedAlarm;
        if (status.isHistorical()) {
            closedAlarm = ((OpenDeviceAlarm) lockedAlarm).close(status);
        } else {
            throw exceptionFactory.newException(MessageSeeds.BAD_FIELD_VALUE, "status.id");
        }
        return closedAlarm != null ? deviceAlarmInfoFactory.from(closedAlarm, uriInfo, fieldSelection.getFields()) : null;
    }


    @POST
    @Transactional
    @Path("/{id}/comment")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public Response addComment(@PathParam("id") long alarmId, IssueCommentShortInfo commentShortInfo, @Context UriInfo uriInfo) {

        DeviceAlarm alarm = deviceAlarmService.findAlarm(alarmId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_ALARM, String.valueOf(alarmId)));
        if (commentShortInfo == null || commentShortInfo.comment == null || commentShortInfo.comment.isEmpty()) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.BAD_FIELD_VALUE, "comment");
        }
        alarm.addComment(commentShortInfo.comment, getCurrentUser()).orElseThrow(() -> new WebApplicationException(Response.Status.BAD_REQUEST));

        URI uri = uriInfo.getBaseUriBuilder().
                path(DeviceAlarmResource.class).
                build();
        return Response.created(uri).build();
    }

    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public List<String> getFields() {
        return deviceAlarmInfoFactory.getAvailableFields().stream().sorted().collect(toList());
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
