package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.bpm.ProcessInstanceInfo;
import com.elster.jupiter.bpm.UserTaskInfo;
import com.elster.jupiter.issue.share.IssueFilter;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
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
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.streams.Predicates.not;

public class GoingOnResource {

    private final ServiceCallService serviceCallService;
    private final BpmService bpmService;
    private final IssueService issueService;
    private final ResourceHelper resourceHelper;
    private final MeteringService meteringService;
    private final Thesaurus thesaurus;
    private final Clock clock;
    private final Optional<IssueStatus> open;
    private final Optional<IssueStatus> inProgress;

    @Inject
    public GoingOnResource(ResourceHelper resourceHelper, ServiceCallService serviceCallService, BpmService bpmService, IssueService issueService, MeteringService meteringService, Clock clock, Thesaurus thesaurus) {
        this.resourceHelper = resourceHelper;
        this.serviceCallService = serviceCallService;
        this.bpmService = bpmService;
        this.issueService = issueService;
        this.meteringService = meteringService;
        this.clock = clock;
        this.thesaurus = thesaurus;
        this.open = issueService.findStatus(IssueStatus.OPEN);
        this.inProgress = issueService.findStatus(IssueStatus.IN_PROGRESS);

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response getGoingOn(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters, @Context SecurityContext securityContext, @HeaderParam("Authorization") String auth, @HeaderParam("X-CONNEXO-APPLICATION-NAME") String appKey) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);

        GoingOnInfoFactory goingOnInfoFactory = new GoingOnInfoFactory(null);
        if (securityContext.getUserPrincipal() instanceof User) {
            goingOnInfoFactory = new GoingOnInfoFactory((User) securityContext.getUserPrincipal());
        }

        Optional<AmrSystem> amrSystem = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId());
        Optional<Meter> meter = amrSystem.get().findMeter(String.valueOf(device.getId()));
        IssueFilter issueFilter = issueService.newIssueFilter();
        issueFilter.addDevice(meter.get());
        open.ifPresent(issueFilter::addStatus);
        inProgress.ifPresent(issueFilter::addStatus);
        List<GoingOnInfo> issues = issueService.findIssues(issueFilter)
                .stream()
                .map(goingOnInfoFactory::toGoingOnInfo)
                .collect(Collectors.toList());

        List<GoingOnInfo> serviceCalls = serviceCallService.findServiceCalls(device, serviceCallService.nonFinalStates())
                .stream()
                .map(goingOnInfoFactory::toGoingOnInfo)
                .collect(Collectors.toList());

        List<GoingOnInfo> processInstances = bpmService.getRunningProcesses(auth, filterFor(device), appKey)
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

    private class GoingOnInfoFactory {

        private final User currentUser;

        private GoingOnInfoFactory(User currentUser) {
            this.currentUser = currentUser;
        }

        private GoingOnInfo toGoingOnInfo(Issue issue) {
            GoingOnInfo goingOnInfo = new GoingOnInfo();
            goingOnInfo.type = "issue";
            goingOnInfo.issueType = issue.getReason().getIssueType().getKey();
            goingOnInfo.id = issue.getId();
            goingOnInfo.reference = null;
            goingOnInfo.description = issue.getReason().getName();
            goingOnInfo.dueDate = issue.getDueDate();
            goingOnInfo.severity = severity(issue.getDueDate());
            goingOnInfo.assignee = Optional.ofNullable(issue.getAssignee()).filter(issueAssignee -> issueAssignee.getUser() != null).map(issueAssignee -> issueAssignee.getUser().getName()).orElse(null);
            goingOnInfo.assigneeIsCurrentUser = Optional.ofNullable(issue.getAssignee()).filter(issueAssignee -> issueAssignee.getUser() != null).map(issueAssignee -> issueAssignee.getUser().getId()).map(id -> id.equals(currentUser.getId())).orElse(false);
            goingOnInfo.status = issue.getStatus().getName();
            return goingOnInfo;
        }

        private GoingOnInfo toGoingOnInfo(ServiceCall serviceCall) {
            GoingOnInfo goingOnInfo = new GoingOnInfo();
            goingOnInfo.type = "servicecall";
            goingOnInfo.issueType = null;
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
            goingOnInfo.issueType = null;
            goingOnInfo.id = Long.parseLong(processInstanceInfo.processId);
            goingOnInfo.reference = null;
            goingOnInfo.description = processInstanceInfo.name;
            goingOnInfo.dueDate = userTaskInfo.flatMap(info -> Optional.ofNullable(info.dueDate)).filter(not(String::isEmpty)).map(Long::parseLong).map(Instant::ofEpochMilli).orElse(null);
            goingOnInfo.severity = severity(goingOnInfo.dueDate);
            goingOnInfo.assignee = processInstanceInfo.startedBy;
            goingOnInfo.assigneeIsCurrentUser = userTaskInfo.flatMap(info -> Optional.ofNullable(info.isAssignedToCurrentUser)).orElse(false);
            goingOnInfo.status = processInstanceInfo.status;
            if(goingOnInfo.status != null){
                switch (goingOnInfo.status) {
                    case "0":
                        goingOnInfo.status = thesaurus.getString("Open", "Pending");
                        break;
                    case "1":
                        goingOnInfo.status = thesaurus.getString("Open", "Active");
                        break;
                    case "2":
                        goingOnInfo.status = thesaurus.getString("Open", "Completed");
                        break;
                    case "3":
                        goingOnInfo.status = thesaurus.getString("Open", "Aborted");
                        break;
                    case "4":
                        goingOnInfo.status = thesaurus.getString("Open", "Suspended");
                        break;
                }
            }
            return goingOnInfo;
        }

        private Severity severity(Instant dueDate){
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
