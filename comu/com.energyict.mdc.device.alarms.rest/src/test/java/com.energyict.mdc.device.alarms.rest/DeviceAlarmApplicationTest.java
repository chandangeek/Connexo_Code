/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.rest;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventOrAction;
import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.elster.jupiter.cbo.EndDeviceType;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.issue.rest.TranslationKeys;
import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.IssueAction;
import com.elster.jupiter.issue.share.IssueGroupFilter;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.DueInType;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueAssignmentService;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyTypeInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.properties.rest.SimplePropertyType;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.WorkGroup;
import com.energyict.mdc.common.device.data.LogBook;
import com.energyict.mdc.common.masterdata.LogBookType;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.energyict.mdc.device.alarms.event.DeviceAlarmRelatedEvent;
import com.energyict.mdc.device.alarms.rest.i18n.DeviceAlarmTranslationKeys;
import com.energyict.mdc.device.data.LogBookService;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.mockito.Matchers;
import org.mockito.Mock;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeviceAlarmApplicationTest extends FelixRestApplicationJerseyTest {

    private final Instant now = ZonedDateTime.of(2016, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC")).toInstant();

    @Mock
    DeviceAlarmService deviceAlarmService;
    @Mock
    LogBookService logBookService;
    @Mock
    MeteringService meteringService;
    @Mock
    MeteringGroupsService meteringGroupsService;
    @Mock
    IssueService issueService;
    @Mock
    UserService userService;
    @Mock
    Thesaurus thesaurus;
    @Mock
    NlsService nlsService;
    @Mock
    BpmService bpmService;
    @Mock
    PropertyValueInfoService propertyValueInfoService;
    @Mock
    IssueActionService issueActionService;
    @Mock
    IssueCreationService issueCreationService;
    @Mock
    IssueAssignmentService issueAssignmentService;
    @Mock
    TimeService timeService;
    @Mock
    static SecurityContext securityContext;
    @Mock
    static Clock clock;

    @Provider
    @Priority(Priorities.AUTHORIZATION)
    private static class SecurityRequestFilter implements ContainerRequestFilter {
        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            requestContext.setSecurityContext(securityContext);
        }
    }

    @Override
    protected Application getApplication() {
        DeviceAlarmApplication deviceAlarmApplication = new DeviceAlarmApplication() {
            //to mock security context
            @Override
            public Set<Class<?>> getClasses() {
                Set<Class<?>> hashSet = new HashSet<>(super.getClasses());
                hashSet.add(SecurityRequestFilter.class);
                return Collections.unmodifiableSet(hashSet);
            }
        };
        when(thesaurus.join(thesaurus)).thenReturn(thesaurus);
        when(nlsService.getThesaurus("DAR", Layer.REST)).thenReturn(thesaurus);
        when(nlsService.getThesaurus(DeviceAlarmService.COMPONENT_NAME, Layer.REST)).thenReturn(thesaurus);
        when(nlsService.getThesaurus(DeviceAlarmService.COMPONENT_NAME, Layer.DOMAIN)).thenReturn(thesaurus);
        when(issueService.getIssueActionService()).thenReturn(issueActionService);
        when(issueService.getIssueCreationService()).thenReturn(issueCreationService);
        when(issueService.getIssueAssignmentService()).thenReturn(issueAssignmentService);

        deviceAlarmApplication.setTransactionService(transactionService);
        deviceAlarmApplication.setDeviceAlarmService(deviceAlarmService);
        deviceAlarmApplication.setLogBookService(logBookService);
        deviceAlarmApplication.setMeteringService(meteringService);
        deviceAlarmApplication.setMeteringGroupsService(meteringGroupsService);
        deviceAlarmApplication.setIssueService(issueService);
        deviceAlarmApplication.setUserService(userService);
        deviceAlarmApplication.setPropertyValueInfoService(propertyValueInfoService);
        deviceAlarmApplication.setNlsService(nlsService);
        deviceAlarmApplication.setBpmService(bpmService);
        deviceAlarmApplication.setTimeService(timeService);
        deviceAlarmApplication.setClock(Clock.fixed(LocalDateTime.of(2017, 1, 1, 16, 0, 0).toInstant(ZoneOffset.UTC), ZoneId.systemDefault()));
        deviceAlarmApplication.setPropertyValueInfoService(propertyValueInfoService);
        deviceAlarmApplication.setTimeService(timeService);
        deviceAlarmApplication.setClock(clock);
        return deviceAlarmApplication;
    }

    protected IssueStatus mockStatus(String key, String name, boolean isFinal) {
        IssueStatus status = mock(IssueStatus.class);
        when(status.isHistorical()).thenReturn(isFinal);
        when(status.getName()).thenReturn(name);
        when(status.getKey()).thenReturn(key);
        return status;
    }

    protected void mockTranslation(DeviceAlarmTranslationKeys translationKey) {
        NlsMessageFormat nlsMessageFormat = this.mockNlsMessageFormat(translationKey.getDefaultFormat());
        when(thesaurus.getFormat(translationKey)).thenReturn(nlsMessageFormat);
    }

    protected CreationRuleTemplate getDefaultCreationRuleTemplate() {
        IssueType issueType = mock(IssueType.class);
        when(issueType.getKey()).thenReturn("devicealarm");
        when(issueType.getName()).thenReturn("Device akarm");
        when(issueType.getPrefix()).thenReturn("DAL");
        return mockCreationRuleTemplate("0-1-2", "Description", issueType, mockPropertySpecs());
    }

    protected List<PropertySpec> mockPropertySpecs() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn("property");
        when(propertySpec.getValueFactory()).thenReturn(new StringFactory());
        when(propertySpec.isRequired()).thenReturn(true);
        return Collections.singletonList(propertySpec);
    }

    protected IssueActionType getDefaultIssueActionType() {
        IssueType issueType = getDefaultIssueType();
        return mockIssueActionType(1, "send", issueType);
    }

    protected IssueActionType mockIssueActionType(long id, String name, IssueType issueType) {
        IssueActionType type = mock(IssueActionType.class);
        IssueAction action = mockIssueAction(name);
        when(type.getId()).thenReturn(id);
        when(type.createIssueAction()).thenReturn(Optional.of(action));
        when(type.getIssueType()).thenReturn(issueType);
        return type;
    }

    protected IssueAction mockIssueAction(String name) {
        IssueAction action = mock(IssueAction.class);
        when(action.getDisplayName()).thenReturn(name);
        List<PropertySpec> propertySpec = mockPropertySpecs();
        when(action.getPropertySpecs()).thenReturn(propertySpec);
        return action;
    }

    protected IssueType getDefaultIssueType() {
        return mockIssueType("devicealarm", "Device alarm");
    }

    protected CreationRuleTemplate mockCreationRuleTemplate(String name, String description, IssueType issueType, List<PropertySpec> properties) {
        CreationRuleTemplate template = mock(CreationRuleTemplate.class);
        when(template.getName()).thenReturn(name);
        when(template.getDisplayName()).thenReturn("Display Name: " + name);
        when(template.getDescription()).thenReturn(description);
        when(template.getIssueType()).thenReturn(issueType);
        when(template.getPropertySpecs()).thenReturn(properties);
        PropertyTypeInfo propertyTypeInfo = new PropertyTypeInfo();
        propertyTypeInfo.simplePropertyType = SimplePropertyType.TEXTAREA;
        PropertyInfo propertyInfo = new PropertyInfo("property", "property", null, propertyTypeInfo, false);
        if (properties != null && !properties.isEmpty()) {
            when(propertyValueInfoService.getPropertyInfos(template.getPropertySpecs())).thenReturn(Collections.singletonList(propertyInfo));
        }
        return template;
    }

    protected IssueType mockIssueType(String key, String name) {
        IssueType issueType = mock(IssueType.class);
        when(issueType.getKey()).thenReturn(key);
        when(issueType.getName()).thenReturn(name);
        when(issueType.getPrefix()).thenReturn(name + key);
        return issueType;
    }

    protected CreationRule mockCreationRule(long id, String name) {
        Instant instant = Instant.now();
        IssueReason reason = getDefaultReason();
        CreationRuleTemplate template = getDefaultCreationRuleTemplate();
        CreationRule rule = mock(CreationRule.class);
        when(rule.getId()).thenReturn(id);
        when(rule.getName()).thenReturn(name);
        when(rule.getComment()).thenReturn("comment");
        when(rule.getReason()).thenReturn(reason);
        when(rule.getDueInType()).thenReturn(DueInType.DAY);
        when(rule.getDueInValue()).thenReturn(5L);
        when(rule.getActions()).thenReturn(Collections.emptyList());
        when(rule.getProperties()).thenReturn(Collections.emptyMap());
        when(rule.getTemplate()).thenReturn(template);
        when(rule.getModTime()).thenReturn(instant);
        when(rule.getCreateTime()).thenReturn(instant);
        when(rule.getPriority()).thenReturn(com.elster.jupiter.issue.share.Priority.DEFAULT);
        when(rule.getVersion()).thenReturn(2L);
        return rule;
    }

    protected void mockTranslation(TranslationKeys translationKey) {
        NlsMessageFormat nlsMessageFormat = this.mockNlsMessageFormat(translationKey.getDefaultFormat());
        when(thesaurus.getFormat(translationKey)).thenReturn(nlsMessageFormat);
    }

    protected NlsMessageFormat mockNlsMessageFormat(String translation) {
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn(translation);
        return messageFormat;
    }

    protected User mockUser(long id, String name) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        when(user.getName()).thenReturn(name);
        return user;
    }

    protected IssueStatus getDefaultStatus() {
        return mockStatus("1", "open", false);
    }

    protected IssueReason mockReason(String key, String name) {
        IssueType issueType = mock(IssueType.class);
        when(issueType.getKey()).thenReturn("devicealarm");
        when(issueType.getName()).thenReturn(name);
        when(issueType.getPrefix()).thenReturn(name + key);
        IssueReason reason = mock(IssueReason.class);
        when(reason.getKey()).thenReturn(key);
        when(reason.getName()).thenReturn(name);
        when(reason.getIssueType()).thenReturn(issueType);
        return reason;
    }

    protected Meter mockMeter(long id, String name) {
        Meter meter = mock(Meter.class);
        when(meter.getName()).thenReturn(name);
        when(meter.getId()).thenReturn(id);
        when(meter.getSerialNumber()).thenReturn("0.0.0.0.0.0.0.0");
        when(meter.getAmrId()).thenReturn(String.valueOf(id));
        doReturn(Optional.empty()).when(meter).getCurrentMeterActivation();
        AmrSystem amrSystem = mock(AmrSystem.class);
        when(meter.getAmrSystem()).thenReturn(amrSystem);
        when(amrSystem.is(KnownAmrSystem.MDC)).thenReturn(true);
        return meter;
    }

    protected Thesaurus getThesaurus() {
        return this.thesaurus;
    }

    protected IssueReason getDefaultReason() {
        return mockReason("1", "Reason");
    }


    protected IssueAssignee mockAssignee(long userId, String userName, long workGroupId, String workGroupName) {
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

    protected Meter mockDevice(long id, String name) {
        Meter meter = mock(Meter.class);
        when(meter.getId()).thenReturn(id);
        when(meter.getAmrId()).thenReturn(String.valueOf(id));
        when(meter.getSerialNumber()).thenReturn("0.0.0.0.0.0.0.0");
        Optional<? extends MeterActivation> optionalMA = Optional.empty();
        doReturn(optionalMA).when(meter).getCurrentMeterActivation();
        AmrSystem amrSystem = mock(AmrSystem.class);
        when(meter.getAmrSystem()).thenReturn(amrSystem);
        when(amrSystem.is(KnownAmrSystem.MDC)).thenReturn(true);
        return meter;
    }

    protected DeviceAlarm getDefaultAlarm() {
        return mockAlarm(1L, getDefaultReason(), getDefaultStatus(), getDefaultAssignee(), getDeviceAlarmRelatedEvent(), mockDevice(1, "DefaultDevice"));
    }

    protected DeviceAlarm mockAlarm(long id, IssueReason reason, IssueStatus status, IssueAssignee assingee, List<DeviceAlarmRelatedEvent> events, Meter meter) {
        DeviceAlarm alarm = mock(DeviceAlarm.class, RETURNS_DEEP_STUBS);
        when(alarm.getId()).thenReturn(id);
        when(alarm.getIssueId()).thenReturn("ALM-001");
        when(alarm.getReason()).thenReturn(reason);
        when(alarm.getStatus()).thenReturn(status);
        when(alarm.getDueDate()).thenReturn(null);
        when(alarm.getDevice()).thenReturn(meter);
        when(alarm.getAssignee()).thenReturn(assingee);
        when(alarm.getCreateTime()).thenReturn(Instant.EPOCH);
        when(alarm.getCreateDateTime()).thenReturn(Instant.EPOCH);
        when(alarm.getModTime()).thenReturn(Instant.EPOCH);
        when(alarm.getVersion()).thenReturn(1L);
        when(alarm.getDeviceAlarmRelatedEvents()).thenReturn(events);
        when(alarm.getSnoozeDateTime()).thenReturn(Optional.of(Instant.EPOCH));
        when(alarm.getDevice().getLocation()).thenReturn(Optional.empty());

        when(alarm.getPriority()).thenReturn(com.elster.jupiter.issue.share.Priority.DEFAULT);
        return alarm;
    }

    protected List<DeviceAlarmRelatedEvent> getDeviceAlarmRelatedEvent() {
        DeviceAlarmRelatedEvent event = mock(DeviceAlarmRelatedEvent.class);
        EndDeviceEventRecord eventRecord = mock(EndDeviceEventRecord.class);
        LogBookType logBookType = mock(LogBookType.class);
        LogBook logBook = mock(LogBook.class);
        EndDeviceEventType endDeviceEventType = mock(EndDeviceEventType.class);

        when(event.getEventRecord()).thenReturn(eventRecord);
        when(eventRecord.getCreateTime()).thenReturn(now);
        when(eventRecord.getCreatedDateTime()).thenReturn(now);
        when(eventRecord.getLogBookId()).thenReturn(1L);
        when(logBookService.findById(anyLong())).thenReturn(Optional.of(logBook));
        when(logBook.getId()).thenReturn(1L);
        when(logBook.getLogBookType()).thenReturn(logBookType);
        when(logBookType.getName()).thenReturn("LogBookName");
        when(eventRecord.getEventType()).thenReturn(endDeviceEventType);
        when(endDeviceEventType.getType()).thenReturn(EndDeviceType.COLLECTOR);
        when(endDeviceEventType.getDomain()).thenReturn(EndDeviceDomain.BATTERY);
        when(endDeviceEventType.getSubDomain()).thenReturn(EndDeviceSubDomain.ACTIVATION);
        when(endDeviceEventType.getEventOrAction()).thenReturn(EndDeviceEventOrAction.ABORTED);

        return Collections.singletonList(event);
    }

    protected IssueGroupFilter mockIssueGroupFilter() {
        IssueGroupFilter issueGroupFilter = mock(IssueGroupFilter.class);
        when(issueGroupFilter.using(Matchers.<Class>anyObject())).thenReturn(issueGroupFilter);
        when(issueGroupFilter.withReasons(Matchers.<List<String>>anyObject())).thenReturn(issueGroupFilter);
        when(issueGroupFilter.groupBy(Matchers.<String>anyObject())).thenReturn(issueGroupFilter);
        when(issueGroupFilter.to(2L)).thenReturn(issueGroupFilter);
        return issueGroupFilter;
    }
}
