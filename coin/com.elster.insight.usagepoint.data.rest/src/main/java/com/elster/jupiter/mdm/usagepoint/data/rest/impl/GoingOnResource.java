/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.bpm.ProcessInstanceInfo;
import com.elster.jupiter.bpm.UserTaskInfo;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GoingOnResource {

    private final ServiceCallService serviceCallService;
    private final BpmService bpmService;
    private final ResourceHelper resourceHelper;
    private final Clock clock;

    @Inject
    public GoingOnResource(ResourceHelper resourceHelper, ServiceCallService serviceCallService, BpmService bpmService, Clock clock) {
        this.resourceHelper = resourceHelper;
        this.serviceCallService = serviceCallService;
        this.bpmService = bpmService;
        this.clock = clock;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response getGoingOn(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters,
                               @HeaderParam("authorization") String auth, @HeaderParam("X-CONNEXO-APPLICATION-NAME") String appKey) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);

        GoingOnInfoFactory goingOnInfoFactory = new GoingOnInfoFactory();

        List<GoingOnInfo> issues = Collections.emptyList();

        List<GoingOnInfo> serviceCalls = serviceCallService.findServiceCalls(usagePoint, serviceCallService.nonFinalStates())
                .stream()
                .map(goingOnInfoFactory::toGoingOnInfo)
                .collect(Collectors.toList());

        List<GoingOnInfo> processInstances = bpmService.getRunningProcesses(auth, filterFor(usagePoint), appKey)
                .processes
                .stream()
                .map(goingOnInfoFactory::toGoingOnInfo)
                .collect(Collectors.toList());

        List<GoingOnInfo> goingOnInfos = Stream.of(issues, serviceCalls, processInstances)
                .flatMap(List::stream)
                .sorted(GoingOnInfo.order())
                .collect(Collectors.toList());

        return Response.ok(PagedInfoList.fromPagedList("goingsOn", goingOnInfos, queryParameters)).build();
    }

    private String filterFor(UsagePoint usagePoint) {
        return "?variableid=usagePointId&variablevalue=" + usagePoint.getId();
    }

    private final class GoingOnInfoFactory {

        private GoingOnInfoFactory() {
        }

        private GoingOnInfo toGoingOnInfo(ServiceCall serviceCall) {
            GoingOnInfo goingOnInfo = new GoingOnInfo();
            goingOnInfo.type = "servicecall";
            goingOnInfo.id = serviceCall.getId();
            goingOnInfo.reference = serviceCall.getNumber();
            goingOnInfo.description = serviceCall.getType().getName();
            goingOnInfo.dueDate = null;
            goingOnInfo.severity = null;
            goingOnInfo.assignee = null;
            goingOnInfo.assigneeIsCurrentUser = false;
            goingOnInfo.status = serviceCallService.getDisplayName(serviceCall.getState());
            return goingOnInfo;
        }

        private GoingOnInfo toGoingOnInfo(ProcessInstanceInfo processInstanceInfo) {
            Optional<UserTaskInfo> userTaskInfo = processInstanceInfo.openTasks
                    .stream()
                    .min(Comparator.comparing(info -> Long.parseLong(info.dueDate)));
            GoingOnInfo goingOnInfo = new GoingOnInfo();
            goingOnInfo.type = "process";
            goingOnInfo.id = userTaskInfo.map(info -> Long.parseLong(info.id)).orElse(0L);
            goingOnInfo.reference = null;
            goingOnInfo.description = processInstanceInfo.name;
            goingOnInfo.severity = severity(goingOnInfo.dueDate);
            goingOnInfo.assignee = userTaskInfo.flatMap(info -> Optional.ofNullable(info.actualOwner)).orElse(null);
            goingOnInfo.assigneeIsCurrentUser = userTaskInfo.flatMap(info -> Optional.ofNullable(info.isAssignedToCurrentUser)).orElse(false);
            goingOnInfo.status = userTaskInfo.flatMap(info -> Optional.ofNullable(info.status)).orElse(null);
            return goingOnInfo;
        }

        private Severity severity(Instant dueDate) {
            if (dueDate == null) {
                return null;
            }
            ZonedDateTime dueTime = ZonedDateTime.ofInstant(dueDate, clock.getZone());
            ZonedDateTime now = ZonedDateTime.now(clock);

            if (LocalDate.from(dueTime).equals(LocalDate.from(now)) || LocalDate.from(dueTime).equals(LocalDate.from(now).plusDays(1))) {
                return Severity.WARNING;
            }

            return Optional.of(dueDate).filter(due -> clock.instant().isAfter(due)).map(due -> Severity.HIGH).orElse(null);
        }
    }

}
