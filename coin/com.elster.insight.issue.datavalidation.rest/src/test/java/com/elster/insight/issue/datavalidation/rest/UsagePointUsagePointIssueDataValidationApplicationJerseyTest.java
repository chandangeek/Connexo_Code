/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */


package com.elster.insight.issue.datavalidation.rest;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.ConnectionState;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.GasDetail;
import com.elster.jupiter.metering.HeatDetail;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointConnectionState;
import com.elster.jupiter.metering.UsagePointCustomPropertySetExtension;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.WaterDetail;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.WorkGroup;
import com.elster.jupiter.devtools.tests.Matcher;


import com.elster.insight.issue.datavalidation.UsagePointOpenIssueDataValidation;
import com.elster.insight.issue.datavalidation.UsagePointIssueDataValidationService;
import com.elster.insight.issue.datavalidation.rest.impl.UsagePointIssueDataValidationApplication;

import com.google.common.collect.Range;

import javax.ws.rs.core.Application;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Optional;

import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.longThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class UsagePointUsagePointIssueDataValidationApplicationJerseyTest extends FelixRestApplicationJerseyTest {
    @Mock
    UserService userService;
    @Mock
    IssueService issueService;
    @Mock
    UsagePointIssueDataValidationService usagePointIssueDataValidationService;
    @Mock
    IssueActionService issueActionService;
    @Mock
    MeteringService meteringService;
    @Mock
    Clock clock;

    @Override
    protected Application getApplication() {
        UsagePointIssueDataValidationApplication application = new UsagePointIssueDataValidationApplication();
        when(issueService.getIssueActionService()).thenReturn(issueActionService);
        application.setIssueService(issueService);
        application.setUsagePointIssueDataValidationService(usagePointIssueDataValidationService);
        application.setMeteringService(meteringService);
        application.setUserService(userService);
        application.setTransactionService(transactionService);
        application.setNlsService(nlsService);
        return application;
    }

    protected IssueStatus mockStatus(String key, String name, boolean isFinal) {
        IssueStatus status = mock(IssueStatus.class);
        when(status.isHistorical()).thenReturn(isFinal);
        when(status.getName()).thenReturn(name);
        when(status.getKey()).thenReturn(key);
        when(issueService.findStatus(key)).thenReturn(Optional.of(status));
        return status;
    }

    protected IssueStatus getDefaultStatus() {
        return mockStatus("1", "open", false);
    }

    protected IssueType mockIssueType(String key, String name) {
        IssueType issueType = mock(IssueType.class);
        when(issueType.getKey()).thenReturn(key);
        when(issueType.getName()).thenReturn(name);
        when(issueService.findIssueType(key)).thenReturn(Optional.of(issueType));
        return issueType;
    }

    protected IssueType getDefaultIssueType() {
        return mockIssueType("usagepointdatavalidation", "Usage Point Data Validation");
    }

    protected IssueReason mockReason(String key, String name, IssueType issueType) {
        IssueReason reason = mock(IssueReason.class);
        when(reason.getKey()).thenReturn(key);
        when(reason.getName()).thenReturn(name);
        when(reason.getIssueType()).thenReturn(issueType);
        when(issueService.findReason(key)).thenReturn(Optional.of(reason));
        return reason;
    }

    protected IssueReason getDefaultReason() {
        return mockReason("1", "Reason", getDefaultIssueType());
    }

    protected Meter mockMeter(long id, String name) {
        Meter meter = mock(Meter.class);
        when(meter.getId()).thenReturn(id);
        when(meter.getName()).thenReturn(name);
        when(meter.getAmrId()).thenReturn(String.valueOf(id));
        Optional<? extends MeterActivation> optionalMA = Optional.empty();
        doReturn(optionalMA).when(meter).getCurrentMeterActivation();
        AmrSystem amrSystem = mock(AmrSystem.class);
        when(meter.getAmrSystem()).thenReturn(amrSystem);
        when(meter.getSerialNumber()).thenReturn("0.0.0.0.0.0.0.0");
        when(amrSystem.is(KnownAmrSystem.MDC)).thenReturn(true);
        when(meteringService.findEndDeviceById(id)).thenReturn(Optional.of(meter));
        when(meteringService.findMeterByName(name)).thenReturn(Optional.of(meter));
        return meter;
    }

    protected Meter getDefaultDevice() {
        return mockMeter(1, "DefaultDevice");
    }

    protected IssueAssignee mockAssignee(long userId, String userName, long workGroupId, String workGroupName){
        IssueAssignee assignee = mock(IssueAssignee.class);
        User user = mock(User.class);
        WorkGroup workGroup = mock(WorkGroup.class);
        when(workGroup.getId()).thenReturn(workGroupId);
        when(workGroup.getName()).thenReturn(workGroupName);
        when(user.getId()).thenReturn(userId);
        when(user.getName()).thenReturn(userName);
        when(assignee.getUser()).thenReturn(user);
        when(assignee.getWorkGroup()).thenReturn(workGroup);
        return assignee;
    }

    protected IssueAssignee getDefaultAssignee() {
        return mockAssignee(1L, "Admin", 1L, "WorkGroup");
    }

    protected User mockUser(long id, String name) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        when(user.getName()).thenReturn(name);
        when(userService.getUser(id)).thenReturn(Optional.of(user));
        return user;
    }

    protected UsagePointOpenIssueDataValidation getDefaultIssue() {
        return mockIssue(1L, getDefaultReason(), getDefaultStatus(), getDefaultAssignee(), getDefaultDevice(), getDefaultUsagePoint());
    }

    protected UsagePoint getDefaultUsagePoint(){
        return mockUsagePoint("1", 1L, ServiceKind.ELECTRICITY);
    }

    protected UsagePointOpenIssueDataValidation mockIssue(long id, IssueReason reason, IssueStatus status, IssueAssignee assingee, Meter meter, UsagePoint usagePoint) {
        UsagePointOpenIssueDataValidation issue = mock(UsagePointOpenIssueDataValidation.class,RETURNS_DEEP_STUBS);
        when(issue.getId()).thenReturn(id);
        when(issue.getReason()).thenReturn(reason);
        when(issue.getStatus()).thenReturn(status);
        when(issue.getDueDate()).thenReturn(null);
        when(issue.getAssignee()).thenReturn(assingee);
        when(issue.getDevice()).thenReturn(meter);
        when(issue.getUsagePoint()).thenReturn(Optional.of(usagePoint));
        when(issue.getCreateTime()).thenReturn(Instant.EPOCH);
        when(issue.getCreateDateTime()).thenReturn(Instant.EPOCH);
        when(issue.getModTime()).thenReturn(Instant.EPOCH);
        when(issue.getVersion()).thenReturn(1L);
        when(issue.getPriority()).thenReturn(Priority.DEFAULT);
        when(issue.getSnoozeDateTime()).thenReturn(Optional.empty());
        when(issue.getDevice().getLocation()).thenReturn(Optional.empty());
        return issue;
    }


    protected UsagePoint mockUsagePoint(String mRID, long version, ServiceKind serviceKind) {
        UsagePointCustomPropertySetExtension extension = mock(UsagePointCustomPropertySetExtension.class);
        when(extension.getAllPropertySets()).thenReturn(Collections.emptyList());
        UsagePointDetail detail;
        switch (serviceKind) {
            case ELECTRICITY:
                detail = mock(ElectricityDetail.class);
                break;
            case GAS:
                detail = mock(GasDetail.class);
                break;
            case WATER:
                detail = mock(WaterDetail.class);
                break;
            case HEAT:
                detail = mock(HeatDetail.class);
                break;
            default:
                throw new IllegalArgumentException("Service kind is not supported");
        }
        when(detail.getRange()).thenReturn(Range.atLeast(clock.instant()));
        return mockUsagePoint(mRID, version, extension, serviceKind, detail);
    }

    private UsagePoint mockUsagePoint(String mRID, long version, UsagePointCustomPropertySetExtension extension, ServiceKind serviceKind, UsagePointDetail detail) {
        UsagePoint usagePoint = mock(UsagePoint.class);
        when(usagePoint.getLocation()).thenReturn(Optional.empty());
        when(usagePoint.getVersion()).thenReturn(version);
        when(usagePoint.getMRID()).thenReturn(mRID);
        when(usagePoint.getAliasName()).thenReturn("alias " + mRID);
        when(usagePoint.getDescription()).thenReturn("usage point desc");
        when(usagePoint.getOutageRegion()).thenReturn("outage region");
        when(usagePoint.getReadRoute()).thenReturn("read route");
        when(usagePoint.getServiceLocationString()).thenReturn("location");
        ServiceCategory serviceCategory = mock(ServiceCategory.class);
        when(serviceCategory.getKind()).thenReturn(serviceKind);
        when(usagePoint.getServiceCategory()).thenReturn(serviceCategory);
        doReturn(Optional.ofNullable(detail)).when(usagePoint).getDetail(any(Instant.class));
        doReturn(Collections.singletonList(detail)).when(usagePoint).getDetails();
        doReturn(Collections.singletonList(detail)).when(usagePoint).getDetail(eq(Range.all()));
        when(usagePoint.getInstallationTime()).thenReturn(LocalDateTime.of(2016, 3, 20, 11, 0).toInstant(ZoneOffset.UTC));
        when(usagePoint.getServiceDeliveryRemark()).thenReturn("remark");
        when(usagePoint.getServicePriority()).thenReturn("service priority");
        when(usagePoint.getEffectiveMetrologyConfiguration(any())).thenReturn(Optional.empty());
        when(usagePoint.getMeterActivations()).thenReturn(Collections.emptyList());
        UsagePointConnectionState usagePointConnectionState = mockUsagePointConnectionState(ConnectionState.CONNECTED);
        when(usagePoint.getCurrentConnectionState()).thenReturn(Optional.of(usagePointConnectionState));

        when(usagePoint.forCustomProperties()).thenReturn(extension);
        when(meteringService.findUsagePointByMRID(mRID)).thenReturn(Optional.of(usagePoint));
        when(meteringService.findAndLockUsagePointByMRIDAndVersion(eq(mRID), longThat(Matcher.matches(v -> v != version)))).thenReturn(Optional.empty());
        when(meteringService.findAndLockUsagePointByMRIDAndVersion(mRID, version)).thenReturn(Optional.of(usagePoint));
        when(detail.getUsagePoint()).thenReturn(usagePoint);
        return usagePoint;
    }

    protected UsagePointConnectionState mockUsagePointConnectionState(ConnectionState connectionState) {
        UsagePointConnectionState usagePointConnectionState = mock(UsagePointConnectionState.class);
        when(usagePointConnectionState.getConnectionState()).thenReturn(connectionState);
        return usagePointConnectionState;
    }
}
