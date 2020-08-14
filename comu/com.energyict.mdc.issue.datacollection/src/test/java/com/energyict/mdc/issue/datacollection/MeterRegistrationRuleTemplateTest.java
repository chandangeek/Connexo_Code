/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.issue.impl.records.HistoricalIssueImpl;
import com.elster.jupiter.issue.impl.records.OpenIssueImpl;
import com.elster.jupiter.issue.impl.service.IssueServiceImpl;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.IssueProvider;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.time.TimeService;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;
import com.energyict.mdc.issue.datacollection.event.UnregisteredFromGatewayDelayedEvent;
import com.energyict.mdc.issue.datacollection.impl.ModuleConstants;
import com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription;
import com.energyict.mdc.issue.datacollection.impl.records.OpenIssueDataCollectionImpl;
import com.energyict.mdc.issue.datacollection.impl.templates.MeterRegistrationRuleTemplate;

import com.google.inject.Injector;
import org.osgi.service.event.EventConstants;

import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MeterRegistrationRuleTemplateTest extends BaseTest {

    @Test
    @Transactional
    public void testCanCreateIssue() {
        CreationRule rule = getCreationRule("testCanCreateIssue", ModuleConstants.REASON_UNREGISTERED_DEVICE);
        Meter meter = createMeter("1", "Name");
        OpenIssue baseIssue = createBaseIssue(rule, meter);

        MeterRegistrationRuleTemplate template = getInjector().getInstance(MeterRegistrationRuleTemplate.class);
        UnregisteredFromGatewayDelayedEvent event = getUnregisteredFromGatewayEvent(1L, 2L);

        OpenIssueDataCollection issue = template.createIssue(baseIssue, event);
        assertThat(issue.getId()).isEqualTo(baseIssue.getId());
        assertThat(issue.getDeviceIdentification()).isEqualTo("1");
        assertThat(issue.getLastGatewayIdentification()).isEqualTo("2");
    }

    @Test
    @Transactional
    public void testInProgressToOpenTransition() {
        CreationRule rule = getCreationRule("testInProgressToOpenTransition", ModuleConstants.REASON_UNREGISTERED_DEVICE);
        Meter meter = createMeter("1", "Name");
        OpenIssue baseIssue = createBaseIssue(rule, meter);
        OpenIssueDataCollectionImpl idcIssue = getDataModel().getInstance(OpenIssueDataCollectionImpl.class);
        idcIssue.setIssue(baseIssue);
        idcIssue.setDeviceIdentification("1");
        idcIssue.setStatus(getIssueService().findStatus(IssueStatus.IN_PROGRESS).get());
        idcIssue.save();

        MeterRegistrationRuleTemplate template = getInjector().getInstance(MeterRegistrationRuleTemplate.class);
        UnregisteredFromGatewayDelayedEvent event = getUnregisteredFromGatewayEvent(1L, 2L);
        template.updateIssue(idcIssue, event);

        Optional<OpenIssueDataCollection> openIssue = getIssueDataCollectionService().findOpenIssue(idcIssue.getId());
        assertThat(openIssue.isPresent()).isTrue();
        assertThat(openIssue.get().getStatus().getKey()).isEqualTo(IssueStatus.OPEN);
    }

    @Test
    @Transactional
    public void testResolveIssue() {
        // Create base issue
        CreationRule rule = getCreationRule("testResolveIssue", ModuleConstants.REASON_UNREGISTERED_DEVICE);
        Meter meter = createMeter("1", "Name");
        Issue baseIssue = createBaseIssue(rule, meter);
        // Create data-collection issue
        OpenIssueDataCollectionImpl idcIssue = getDataModel().getInstance(OpenIssueDataCollectionImpl.class);
        idcIssue.setIssue((OpenIssue) baseIssue);
        idcIssue.setDeviceIdentification("1");
        idcIssue.save();
        // Mock event
        IssueEvent event = mock(IssueEvent.class);
        Optional<Issue> issueRef = Optional.of(idcIssue);
        doReturn(issueRef).when(event).findExistingIssue();
        // Test template
        MeterRegistrationRuleTemplate template = getInjector().getInstance(MeterRegistrationRuleTemplate.class);
        assertThat(template.resolveIssue(event).isPresent()).isTrue();
    }

    @Test
    @Transactional
    public void testCloseBaseIssue() {
        ((IssueServiceImpl) getIssueService()).addIssueProvider((IssueProvider) getIssueDataCollectionService());

        CreationRule rule = getCreationRule("testCanCreateIssue", ModuleConstants.REASON_UNREGISTERED_DEVICE);
        Meter meter = createMeter("1", "Name");
        MeterRegistrationRuleTemplate template = getInjector().getInstance(MeterRegistrationRuleTemplate.class);
        UnregisteredFromGatewayDelayedEvent event = getUnregisteredFromGatewayEvent(1L, 2L);
        OpenIssue issue = template.createIssue(createBaseIssue(rule, meter), event);
        Optional<? extends Issue> baseIssue = getIssueService().findIssue(issue.getId());
        assertThat(baseIssue.get() instanceof OpenIssueImpl).isTrue();
        ((OpenIssue) baseIssue.get()).close(getIssueService().findStatus(IssueStatus.WONT_FIX).get());
        baseIssue = getIssueService().findIssue(issue.getId());
        assertThat(baseIssue.get() instanceof HistoricalIssueImpl).isTrue();
        assertThat(baseIssue.get().getStatus().getKey()).isEqualTo(IssueStatus.WONT_FIX);
    }

    private Meter createMeter(String amrId, String name) {
        AmrSystem amrSystem = getMeteringService().findAmrSystem(1).get();
        return amrSystem.newMeter(amrId, name).create();
    }

    private OpenIssue createBaseIssue(CreationRule rule, Meter meter) {
        DataModel isuDataModel = getIssueDataModel();
        OpenIssueImpl baseIssue = isuDataModel.getInstance(OpenIssueImpl.class);
        baseIssue.setStatus(getIssueService().findStatus(IssueStatus.OPEN).get());
        baseIssue.setReason(rule.getReason());
        baseIssue.setDevice(meter);
        baseIssue.setRule(rule);
        baseIssue.setPriority(Priority.DEFAULT);
        baseIssue.save();
        return baseIssue;
    }

    private UnregisteredFromGatewayDelayedEvent getUnregisteredFromGatewayEvent(Long amrId, Long masterAmrId) {
        DeviceService mockDeviceDataService = mock(DeviceService.class);
        TopologyService mockTopologyService = mock(TopologyService.class);
        TimeService timeService = mock(TimeService.class);
        final Clock clock = mock(Clock.class);
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(amrId);
        when(device.getmRID()).thenReturn(amrId.toString());
        Device master = mock(Device.class);
        when(master.getId()).thenReturn(masterAmrId);
        when(master.getmRID()).thenReturn(masterAmrId.toString());
        when(mockDeviceDataService.findDeviceById(amrId)).thenReturn(Optional.of(device));
        when(mockDeviceDataService.findDeviceById(masterAmrId)).thenReturn(Optional.of(master));
        UnregisteredFromGatewayDelayedEvent event = new UnregisteredFromGatewayDelayedEvent(device, Optional.of(master), getIssueDataCollectionService(), getMeteringService(), mockDeviceDataService, getCommunicationTaskService(), mockTopologyService, getThesaurus(), mock(Injector.class), timeService, getEventService(), clock, getIssueService());
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put(EventConstants.EVENT_TOPIC, "com/energyict/mdc/outboundcommunication/UNKNOWNSLAVEDEVICE");
        messageMap.put(ModuleConstants.DEVICE_IDENTIFIER, amrId);
        messageMap.put(ModuleConstants.GATEWAY_IDENTIFIER, amrId);
        messageMap.put(EventConstants.TIMESTAMP, Instant.now().toEpochMilli());
        event.wrap(messageMap, DataCollectionEventDescription.UNKNOWN_INBOUND_DEVICE, device);
        return event;
    }
}
