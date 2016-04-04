package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.bpm.ProcessInstanceInfo;
import com.elster.jupiter.bpm.UserTaskInfo;
import com.elster.jupiter.issue.share.IssueFilter;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.users.User;
import com.energyict.mdc.device.data.Device;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GoingOnResource {

    private final ServiceCallService serviceCallService;
    private final BpmService bpmService;
    private final IssueService issueService;
    private final ResourceHelper resourceHelper;
    private final MeteringService meteringService;
    private final Clock clock;

    @Inject
    public GoingOnResource(ResourceHelper resourceHelper, ServiceCallService serviceCallService, BpmService bpmService, IssueService issueService, MeteringService meteringService, Clock clock) {
        this.resourceHelper = resourceHelper;
        this.serviceCallService = serviceCallService;
        this.bpmService = bpmService;
        this.issueService = issueService;
        this.meteringService = meteringService;
        this.clock = clock;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response getGoingOn(@PathParam("mRID") String mrid, @BeanParam JsonQueryParameters queryParameters, @Context SecurityContext securityContext, @HeaderParam("Authorization") String auth) {

        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);

        GoingOnInfoFactory goingOnInfoFactory = new GoingOnInfoFactory(null);
        if (securityContext.getUserPrincipal() instanceof User) {
            goingOnInfoFactory = new GoingOnInfoFactory((User) securityContext.getUserPrincipal());
        }

        Optional<AmrSystem> amrSystem = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId());
        Optional<Meter> meter = amrSystem.get().findMeter(String.valueOf(device.getId()));
        IssueFilter issueFilter = issueService.newIssueFilter();
        issueFilter.addDevice(meter.get());
        List<GoingOnInfo> issues = issueService.findIssues(issueFilter)
                .stream()
                .map(goingOnInfoFactory::toGoingOnInfo)
                .collect(Collectors.toList());

        List<GoingOnInfo> serviceCalls = serviceCallService.findServiceCalls(device, nonFinalStates())
                .stream()
                .map(goingOnInfoFactory::toGoingOnInfo)
                .collect(Collectors.toList());

        List<GoingOnInfo> processInstances = bpmService.getRunningProcesses(auth, filterFor(device))
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

    private String filterFor(Device device) {
        return "?variableid=deviceId&variablevalue=" + device.getmRID();
    }

    private EnumSet<DefaultState> nonFinalStates() {
        return EnumSet.of(
                DefaultState.CREATED,
                DefaultState.PENDING,
                DefaultState.SCHEDULED,
                DefaultState.ONGOING,
                DefaultState.PAUSED
        );
    }

    private class GoingOnInfoFactory {

        private final User currentUser;

        private GoingOnInfoFactory(User currentUser) {
            this.currentUser = currentUser;
        }

        private GoingOnInfo toGoingOnInfo(Issue issue) {
            GoingOnInfo goingOnInfo = new GoingOnInfo();
            goingOnInfo.type = "issue";
            goingOnInfo.id = issue.getId();
            goingOnInfo.description = issue.getReason().getName();
            goingOnInfo.dueDate = issue.getDueDate();
            goingOnInfo.severity = Optional.ofNullable(issue.getDueDate()).filter(dueDate -> clock.instant().isAfter(dueDate)).map(dueDate -> Severity.HIGH).orElse(null);
            goingOnInfo.assignee = Optional.ofNullable(issue.getAssignee()).map(IssueAssignee::getName).orElse(null);
            goingOnInfo.assigneeIsCurrentUser = Optional.ofNullable(issue.getAssignee()).map(IssueAssignee::getId).map(id -> id.equals(currentUser.getId())).orElse(false);
            goingOnInfo.status = issue.getStatus().getName();
            return goingOnInfo;
        }

        private GoingOnInfo toGoingOnInfo(ServiceCall serviceCall) {
            GoingOnInfo goingOnInfo = new GoingOnInfo();
            goingOnInfo.type = "servicecall";
            goingOnInfo.id = serviceCall.getId();
            goingOnInfo.description = null;
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
            goingOnInfo.description = processInstanceInfo.name;
            goingOnInfo.dueDate = userTaskInfo.flatMap(info -> Optional.ofNullable(info.dueDate)).map(Long::parseLong).map(Instant::ofEpochMilli).orElse(null);
            goingOnInfo.severity = null;
            goingOnInfo.assignee = userTaskInfo.flatMap(info -> Optional.ofNullable(info.actualOwner)).orElse(null);
            goingOnInfo.assigneeIsCurrentUser = userTaskInfo.flatMap(info -> Optional.ofNullable(info.isAssignedToCurrentUser)).orElse(false);
            goingOnInfo.status = userTaskInfo.flatMap(info -> Optional.ofNullable(info.status)).orElse(null);
            return goingOnInfo;
        }
    }

}
