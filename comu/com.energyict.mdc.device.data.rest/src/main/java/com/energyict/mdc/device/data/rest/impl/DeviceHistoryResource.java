/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.issue.rest.resource.IssueResourceHelper;
import com.elster.jupiter.issue.rest.resource.StandardParametersBean;
import com.elster.jupiter.issue.rest.response.issue.IssueInfo;
import com.elster.jupiter.issue.rest.response.issue.IssueInfoFactoryService;
import com.elster.jupiter.issue.share.IssueProvider;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.conditions.Order;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.issue.rest.request.RequestHelper.LIMIT;
import static com.elster.jupiter.issue.rest.request.RequestHelper.START;

public class DeviceHistoryResource {

    private ResourceHelper resourceHelper;
    private DeviceLifeCycleHistoryInfoFactory deviceLifeCycleHistoryInfoFactory;
    private DeviceFirmwareHistoryInfoFactory deviceFirmwareHistoryInfoFactory;
    private MeterActivationInfoFactory meterActivationInfoFactory;
    private IssueResourceHelper issueResourceHelper;
    private final IssueService issueService;
    private final IssueInfoFactoryService issueInfoFactoryService;

    @Inject
    public DeviceHistoryResource(ResourceHelper resourceHelper, DeviceLifeCycleHistoryInfoFactory deviceLifeCycleStatesHistoryInfoFactory,
                                 DeviceFirmwareHistoryInfoFactory deviceFirmwareHistoryInfoFactory, MeterActivationInfoFactory meterActivationInfoFactory, IssueResourceHelper issueResourceHelper, IssueService issueService, IssueInfoFactoryService issueInfoFactoryService) {
        this.resourceHelper = resourceHelper;
        this.deviceLifeCycleHistoryInfoFactory = deviceLifeCycleStatesHistoryInfoFactory;
        this.deviceFirmwareHistoryInfoFactory = deviceFirmwareHistoryInfoFactory;
        this.meterActivationInfoFactory = meterActivationInfoFactory;
        this.issueResourceHelper = issueResourceHelper;
        this.issueService = issueService;
        this.issueInfoFactoryService = issueInfoFactoryService;
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
    @RolesAllowed({com.elster.jupiter.issue.security.Privileges.Constants.VIEW_ISSUE, com.elster.jupiter.issue.security.Privileges.Constants.ASSIGN_ISSUE, com.elster.jupiter.issue.security.Privileges.Constants.CLOSE_ISSUE, com.elster.jupiter.issue.security.Privileges.Constants.COMMENT_ISSUE, com.elster.jupiter.issue.security.Privileges.Constants.ACTION_ISSUE})
    public PagedInfoList getAllIssues(@BeanParam com.elster.jupiter.issue.rest.resource.StandardParametersBean params, @BeanParam JsonQueryParameters queryParams, @BeanParam JsonQueryFilter filter) {
        //validateMandatory(params, START, LIMIT);
        Finder<? extends Issue> finder = issueService.findIssues(issueResourceHelper.buildFilterFromQueryParameters(filter));
        addSorting(finder, params);
        if (queryParams.getStart().isPresent() && queryParams.getLimit().isPresent()) {
            finder.paged(queryParams.getStart().get(), queryParams.getLimit().get());
        }
        List<? extends Issue> issues = finder.find();
        List<com.elster.jupiter.issue.rest.response.issue.IssueInfo> issueInfos = new ArrayList<>();
        for (Issue baseIssue : issues) {
            for (IssueProvider issueProvider : issueService.getIssueProviders()) {
                Optional<? extends Issue> issueRef = issueProvider.findIssue(baseIssue.getId());
                issueRef.ifPresent(issue -> issueInfos.add(IssueInfo.class.cast(issueInfoFactoryService.getInfoFactoryFor(issue).from(issue))));
            }
        }
        return PagedInfoList.fromPagedList("data", issueInfos, queryParams);
    }

    private void validateMandatory(StandardParametersBean params, String... mandatoryParameters) {
        if (mandatoryParameters != null) {
            for (String mandatoryParameter : mandatoryParameters) {
                String value = params.getFirst(mandatoryParameter);
                if (value == null) {
                    throw new WebApplicationException(Response.Status.BAD_REQUEST);
                }
            }
        }
    }

    private Finder<? extends Issue> addSorting(Finder<? extends Issue> finder, StandardParametersBean parameters) {
        Order[] orders = parameters.getOrder("");
        for (Order order : orders) {
            finder.sorted(order.getName(), order.ascending());
        }
        finder.sorted("id", false);
        return finder;
    }

}
