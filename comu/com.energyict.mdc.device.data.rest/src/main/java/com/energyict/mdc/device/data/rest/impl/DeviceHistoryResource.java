/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.audit.ApplicationType;
import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.audit.AuditDomainType;
import com.elster.jupiter.audit.AuditService;
import com.elster.jupiter.audit.AuditTrailFilter;
import com.elster.jupiter.audit.rest.AuditInfoFactory;
import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.resource.IssueResourceHelper;
import com.elster.jupiter.issue.rest.resource.StandardParametersBean;
import com.elster.jupiter.issue.rest.response.IssueReasonInfo;
import com.elster.jupiter.issue.rest.response.issue.IssueInfo;
import com.elster.jupiter.issue.rest.response.issue.IssueInfoFactoryService;
import com.elster.jupiter.issue.share.IssueFilter;
import com.elster.jupiter.issue.share.IssueProvider;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.security.Privileges;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.issue.security.Privileges.Constants.ACTION_ISSUE;
import static com.elster.jupiter.issue.security.Privileges.Constants.ASSIGN_ISSUE;
import static com.elster.jupiter.issue.security.Privileges.Constants.CLOSE_ISSUE;
import static com.elster.jupiter.issue.security.Privileges.Constants.COMMENT_ISSUE;
import static com.elster.jupiter.issue.security.Privileges.Constants.VIEW_ISSUE;
import static com.elster.jupiter.util.conditions.Where.where;
import static com.energyict.mdc.device.alarms.security.Privileges.Constants.ACTION_ALARM;
import static com.energyict.mdc.device.alarms.security.Privileges.Constants.ASSIGN_ALARM;
import static com.energyict.mdc.device.alarms.security.Privileges.Constants.CLOSE_ALARM;
import static com.energyict.mdc.device.alarms.security.Privileges.Constants.COMMENT_ALARM;
import static com.energyict.mdc.device.alarms.security.Privileges.Constants.VIEW_ALARM;


public class DeviceHistoryResource {

    private ResourceHelper resourceHelper;
    private DeviceLifeCycleHistoryInfoFactory deviceLifeCycleHistoryInfoFactory;
    private DeviceFirmwareHistoryInfoFactory deviceFirmwareHistoryInfoFactory;
    private MeterActivationInfoFactory meterActivationInfoFactory;
    private IssueResourceHelper issueResourceHelper;
    private IssueService issueService;
    private IssueInfoFactoryService issueInfoFactoryService;
    private OrmService ormService;
    private MeteringService meteringService;
    private AuditService auditService;
    private Thesaurus thesaurus;
    private AuditInfoFactory auditInfoFactory;

    @Inject
    public DeviceHistoryResource(ResourceHelper resourceHelper, DeviceLifeCycleHistoryInfoFactory deviceLifeCycleStatesHistoryInfoFactory,
                                 DeviceFirmwareHistoryInfoFactory deviceFirmwareHistoryInfoFactory, MeterActivationInfoFactory meterActivationInfoFactory,
                                 IssueResourceHelper issueResourceHelper, IssueService issueService, IssueInfoFactoryService issueInfoFactoryService,
                                 OrmService ormService, MeteringService meteringService, Thesaurus thesaurus, AuditService auditService, AuditInfoFactory auditInfoFactory) {
        this.resourceHelper = resourceHelper;
        this.deviceLifeCycleHistoryInfoFactory = deviceLifeCycleStatesHistoryInfoFactory;
        this.deviceFirmwareHistoryInfoFactory = deviceFirmwareHistoryInfoFactory;
        this.meterActivationInfoFactory = meterActivationInfoFactory;
        this.issueResourceHelper = issueResourceHelper;
        this.issueService = issueService;
        this.issueInfoFactoryService = issueInfoFactoryService;
        this.ormService = ormService;
        this.meteringService = meteringService;
        this.thesaurus = thesaurus;
        this.auditService = auditService;
        this.auditInfoFactory = auditInfoFactory;
    }

    @GET
    @Transactional
    @Path("/devicelifecyclechanges")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE})
    public Response getDeviceLifeCycleStatesHistory(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        return Response.ok(deviceLifeCycleHistoryInfoFactory.createDeviceLifeCycleChangeInfos(device)).build();
    }

    @GET
    @Transactional
    @Path("/firmwarechanges")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE})
    public Response getFirmwareHistory(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        return Response.ok(deviceFirmwareHistoryInfoFactory.createDeviceFirmwareHistoryInfos(device)).build();
    }

    @GET
    @Transactional
    @Path("/meteractivations")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE})
    public PagedInfoList getMeterActivationsHistory(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        List<MeterActivationInfo> meterActivationInfoList = device.getMeterActivationsMostRecentFirst().stream()
                .map(meterActivation -> meterActivationInfoFactory.asInfo(meterActivation, device))
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("meterActivations", meterActivationInfoList, queryParameters);
    }


    @GET
    @Transactional
    @Path("/issuesandalarms")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({VIEW_ISSUE, ASSIGN_ISSUE, CLOSE_ISSUE, COMMENT_ISSUE, ACTION_ISSUE, VIEW_ALARM, ASSIGN_ALARM, CLOSE_ALARM, COMMENT_ALARM, ACTION_ALARM})
    public PagedInfoList getAllIssues(@PathParam("name") String name, @BeanParam StandardParametersBean params, @BeanParam JsonQueryParameters queryParams, @BeanParam JsonQueryFilter filter) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        IssueFilter issueFilter = issueResourceHelper.buildFilterFromQueryParameters(filter);
        List<EndDevice> endDevices = meteringService.getEndDeviceQuery().select(Condition.TRUE.and(where("amrId").isEqualToIgnoreCase(device.getId())));
        if (!endDevices.isEmpty() && endDevices.size() == 1) {
            issueFilter.addDevice(endDevices.get(0));
        } else {
            throw new IllegalStateException("Incorrect AMR mapping");
        }
        Finder<? extends Issue> issueFinder = findAlarmAndIssues(issueFilter);
        addSorting(issueFinder, params);
        if (queryParams.getStart().isPresent() && queryParams.getLimit().isPresent()) {
            issueFinder.paged(queryParams.getStart().get(), queryParams.getLimit().get());
        }
        List<? extends Issue> issues = issueFinder.find();
        List<IssueInfo> issueInfos = new ArrayList<>();
        for (Issue baseIssue : issues) {
            for (IssueProvider issueProvider : issueService.getIssueProviders()) {
                Optional<? extends Issue> issueRef = issueProvider.findIssue(baseIssue.getId());
                issueRef.ifPresent(issue -> issueInfos.add(IssueInfo.class.cast(issueInfoFactoryService.getInfoFactoryFor(issue).from(issue))));
            }
        }
        return PagedInfoList.fromPagedList("data", issueInfos, queryParams);
    }

    @GET
    @Transactional
    @Path("/audit")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.elster.jupiter.audit.security.Privileges.Constants.VIEW_AUDIT_LOG})
    public PagedInfoList getDeviceAuditTrail(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter filter) {
        return PagedInfoList.fromPagedList("audit", auditService.getAuditTrail(getDeviceAuditTrailFilter(filter, name))
                .from(queryParameters)
                .stream()
                .map(audit -> auditInfoFactory.from(audit, thesaurus))
                .collect(Collectors.toList()), queryParameters);
    }

    @GET
    @Path("/issueandalarmreasons")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({VIEW_ISSUE, ASSIGN_ISSUE, CLOSE_ISSUE, COMMENT_ISSUE, ACTION_ISSUE, VIEW_ALARM, ASSIGN_ALARM, CLOSE_ALARM, COMMENT_ALARM, ACTION_ALARM})
    public Response getReasons(@BeanParam StandardParametersBean params) {
        Query<IssueReason> query = issueService.query(IssueReason.class);
        List<IssueReason> reasons = query.select(Condition.TRUE).stream()
                .sorted(Comparator.<IssueReason, String>comparing(reason -> reason.getIssueType().getPrefix()
                ).thenComparing(IssueReason::getName,
                        String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
        if (params.getFirst("like") != null) {
            reasons = reasons.stream().filter(reason -> reason.getName().toLowerCase().contains(params.getFirst("like").toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Response.ok().entity(reasons.stream().map(ReasonInfo::new).collect(Collectors.toList())).build();
    }

    private Finder<? extends Issue> addSorting(Finder<? extends Issue> finder, StandardParametersBean parameters) {
        Order[] orders = parameters.getOrder("");
        for (Order order : orders) {
            finder.sorted(order);
        }
        finder.sorted("id", false);
        return finder;
    }

    private Finder<? extends Issue> findAlarmAndIssues(IssueFilter filter, Class<?>... eagers) {
        Condition condition = buildConditionFromFilter(filter);
        List<Class<?>> eagerClasses = determineMainApiClass(filter);
        if (eagers != null && eagers.length > 0) {
            eagerClasses.addAll(Arrays.asList(eagers));
        }
        eagerClasses.addAll(Arrays.asList(IssueReason.class, IssueType.class));
        return DefaultFinder.of((Class<Issue>) eagerClasses.remove(0), condition, ormService.getDataModel(IssueService.COMPONENT_NAME)
                .orElseThrow(IllegalStateException::new), eagerClasses.toArray(new Class<?>[eagerClasses.size()]));
    }

    private List<Class<?>> determineMainApiClass(IssueFilter filter) {
        List<Class<?>> eagerClasses = new ArrayList<>();
        List<IssueStatus> statuses = filter.getStatuses();
        if (!statuses.isEmpty() && statuses.stream().noneMatch(IssueStatus::isHistorical)) {
            eagerClasses.add(OpenIssue.class);
        } else if (!statuses.isEmpty() && statuses.stream().allMatch(IssueStatus::isHistorical)) {
            eagerClasses.add(HistoricalIssue.class);
        } else {
            eagerClasses.add(Issue.class);
        }
        return eagerClasses;
    }

    private Condition buildConditionFromFilter(IssueFilter filter) {
        Condition condition = Condition.TRUE;
        //filter by current associated device
        if (!filter.getDevices().isEmpty()) {
            condition = condition.and(where("device").in(filter.getDevices()));
        }
        //filter by reason
        if (!filter.getIssueReasons().isEmpty()) {
            condition = condition.and(where("reason").in(filter.getIssueReasons()));
        }
        //filter by issue types
        if (!filter.getIssueTypes().isEmpty()) {
            condition = condition.and(where("reason.issueType").in(filter.getIssueTypes()));
        }
        //filter by statuses
        if (!filter.getStatuses().isEmpty()) {
            condition = condition.and(where("status").in(filter.getStatuses()));
        }
        //filter by create time
        if (filter.getStartCreateTime() != null) {
            condition = condition.and(where("createDateTime").isGreaterThanOrEqual(filter.getStartCreateTime()));
        }
        if (filter.getEndCreateTime() != null) {
            condition = condition.and(where("createDateTime").isLessThanOrEqual(filter.getEndCreateTime()));
        }
        return condition;
    }

    private AuditTrailFilter getDeviceAuditTrailFilter(JsonQueryFilter filter, String name) {
        AuditTrailFilter auditFilter = auditService.newAuditTrailFilter(ApplicationType.MDC_APPLICATION_KEY);
        if (filter.hasProperty("changedOnFrom")) {
            auditFilter.setChangedOnFrom(filter.getInstant("changedOnFrom"));
        }
        if (filter.hasProperty("changedOnTo")) {
            auditFilter.setChangedOnTo(filter.getInstant("changedOnTo"));
        }
        if (filter.hasProperty("users")) {
            auditFilter.setChangedBy(filter.getStringList("users"));
        }
        auditFilter.setCategories(filter.getStringList(AuditDomainType.DEVICE.name()));
        auditFilter.setDomainContexts(
                Arrays.stream(AuditDomainContextType.values())
                        .filter(auditDomainContextType -> auditDomainContextType.domainType() == AuditDomainType.DEVICE)
                        .collect(Collectors.toList())
        );
        auditFilter.setDomain(resourceHelper.findDeviceByNameOrThrowException(name).getMeter().getId());
        return auditFilter;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private class ReasonInfo extends IssueReasonInfo {
        public String issueType;

        public ReasonInfo(IssueReason reason) {
            super(reason);
            this.issueType = reason.getIssueType().getPrefix();
        }

        public ReasonInfo() {
        }
    }

}
