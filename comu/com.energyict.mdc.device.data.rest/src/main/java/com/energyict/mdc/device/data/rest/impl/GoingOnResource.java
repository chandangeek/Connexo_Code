package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.issue.share.IssueFilter;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.users.User;
import com.energyict.mdc.device.data.Device;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.Optional;
import java.util.stream.Collectors;

public class GoingOnResource {

    private final ServiceCallService serviceCallService;
    private final BpmService bpmService;
    private final IssueService issueService;
    private final ResourceHelper resourceHelper;
    private final MeteringService meteringService;

    @Inject
    public GoingOnResource(ResourceHelper resourceHelper, ServiceCallService serviceCallService, BpmService bpmService, IssueService issueService, MeteringService meteringService) {
        this.resourceHelper = resourceHelper;
        this.serviceCallService = serviceCallService;
        this.bpmService = bpmService;
        this.issueService = issueService;
        this.meteringService = meteringService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Path("")
    public Response getGoingOn(@PathParam("mRID") String mrid, @BeanParam JsonQueryParameters queryParameters, @Context SecurityContext securityContext) {

        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);

        GoingOnInfoFactory goingOnInfoFactory = new GoingOnInfoFactory(null);
        if (securityContext.getUserPrincipal() instanceof User) {
            goingOnInfoFactory = new GoingOnInfoFactory((User) securityContext.getUserPrincipal());
        }

        Optional<AmrSystem> amrSystem = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId());
        Optional<Meter> meter = amrSystem.get().findMeter(String.valueOf(device.getId()));
        IssueFilter issueFilter = issueService.newIssueFilter();
        issueFilter.addDevice(meter.get());
        issueService.findIssues(issueFilter)
                .stream()
                .map(goingOnInfoFactory::toGoingOnInfo)
                .collect(Collectors.toList());


        return null;
//        List<LoadProfileInfo> loadProfileInfos = LoadProfileInfo.from(loadProfilesOnPage);
//        return Response.ok(PagedInfoList.fromPagedList("loadProfiles", loadProfileInfos.stream().sorted((o1, o2) -> o1.name.compareToIgnoreCase(o2.name)).collect(Collectors
//                .toList()), queryParameters)).build();
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
            goingOnInfo.description = issue.getTitle();
            goingOnInfo.dueDate = issue.getDueDate();
            goingOnInfo.severity = Severity.HIGH; // TODO
            goingOnInfo.assignee = Optional.ofNullable(issue.getAssignee()).map(IssueAssignee::getName).orElse(null);
            goingOnInfo.assigneeIsCurrentUser = Optional.ofNullable(issue.getAssignee()).map(IssueAssignee::getId).map(id -> id.equals(currentUser.getId())).orElse(false);
            goingOnInfo.status = issue.getStatus().getName();
            return goingOnInfo;
        }

//        private GoingOnInfo toGoingOnInfo(ServiceCall serviceCall) {
//            GoingOnInfo goingOnInfo = new GoingOnInfo();
//            goingOnInfo.type = "servicecall";
//            goingOnInfo.id = serviceCall.();
//            goingOnInfo.description = issue.getTitle();
//            goingOnInfo.dueDate = issue.getDueDate();
//            goingOnInfo.severity = Severity.HIGH; // TODO
//            goingOnInfo.assignee = Optional.ofNullable(issue.getAssignee()).map(IssueAssignee::getName).orElse(null);
//            goingOnInfo.assigneeIsCurrentUser = issueService. issue.;
//            goingOnInfo.status = issue.getStatus().;
//
//        }
    }

}
