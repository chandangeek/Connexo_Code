package com.energyict.mdc.issue.datacollection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Matchers;
import org.osgi.service.event.EventConstants;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.issue.impl.records.OpenIssueImpl;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;
import com.energyict.mdc.issue.datacollection.event.UnknownSlaveDeviceEvent;
import com.energyict.mdc.issue.datacollection.impl.ModuleConstants;
import com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription;
import com.energyict.mdc.issue.datacollection.impl.records.OpenIssueDataCollectionImpl;
import com.energyict.mdc.issue.datacollection.impl.templates.BasicDataCollectionRuleTemplate;
import com.google.inject.Injector;

public class BasicDataCollectionRuleTemplateTest extends BaseTest {

    @Test
    @Transactional
    public void testCanCreateIssue() {
        CreationRule rule = getCreationRule("testCanCreateIssue", ModuleConstants.REASON_CONNECTION_FAILED);
        Meter meter = createMeter("1", "mrid");
        Issue baseIssue = getBaseIssue(rule, meter);

        BasicDataCollectionRuleTemplate template = getInjector().getInstance(BasicDataCollectionRuleTemplate.class);
        UnknownSlaveDeviceEvent event = getUnknownDeviceEvent(1L);

        assertThat(template.createIssue(baseIssue, event).isPresent()).isTrue();
    }

    @Test
    @Transactional
    public void testCanCreateIssueOnAnotherDevice() {
        CreationRule rule = getCreationRule("testCanCreateIssueOnAnotherDevice", ModuleConstants.REASON_CONNECTION_FAILED);
        Meter meter = createMeter("1", "mrid");
        Issue baseIssue = getBaseIssue(rule, meter);
        baseIssue.save();
        OpenIssueDataCollectionImpl idcIssue = getDataModel().getInstance(OpenIssueDataCollectionImpl.class);
        idcIssue.init(baseIssue);
        idcIssue.setDeviceMRID("001234");
        idcIssue.save();

        meter = createMeter("2", "mrid2");
        baseIssue = getBaseIssue(rule, meter);

        BasicDataCollectionRuleTemplate template = getInjector().getInstance(BasicDataCollectionRuleTemplate.class);
        UnknownSlaveDeviceEvent event = getUnknownDeviceEvent(2L);

        assertThat(template.createIssue(baseIssue, event).isPresent()).isTrue();
    }

    @Test
    @Transactional
    public void testCanNotCreateIssue() {
        CreationRule rule = getCreationRule("testCanNotCreateIssue", ModuleConstants.REASON_UNKNOWN_INBOUND_DEVICE);
        Meter meter = createMeter("1", "mrid");
        Issue baseIssue = getBaseIssue(rule, meter);
        baseIssue.save();

        OpenIssueDataCollectionImpl idcIssue = getDataModel().getInstance(OpenIssueDataCollectionImpl.class);
        idcIssue.init(baseIssue);
        idcIssue.setDeviceMRID("1");
        idcIssue.save();
        baseIssue = getBaseIssue(rule, meter);
        BasicDataCollectionRuleTemplate template = getInjector().getInstance(BasicDataCollectionRuleTemplate.class);
        UnknownSlaveDeviceEvent event = getUnknownDeviceEvent(1L);
        assertThat(template.createIssue(baseIssue, event).isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testInProgressToOpenTransfer() {
        CreationRule rule = getCreationRule("testInProgressToOpenTransfer", ModuleConstants.REASON_UNKNOWN_INBOUND_DEVICE);
        Meter meter = createMeter("1", "mrid");
        Issue baseIssue = getBaseIssue(rule, meter);
        baseIssue.save();

        OpenIssueDataCollectionImpl idcIssue = getDataModel().getInstance(OpenIssueDataCollectionImpl.class);
        idcIssue.init(baseIssue);
        idcIssue.setDeviceMRID("1");
        idcIssue.setStatus(getIssueService().findStatus(IssueStatus.IN_PROGRESS).get());
        idcIssue.save();
        baseIssue = getBaseIssue(rule, meter);
        BasicDataCollectionRuleTemplate template = getInjector().getInstance(BasicDataCollectionRuleTemplate.class);
        UnknownSlaveDeviceEvent event = getUnknownDeviceEvent(1L);
        assertThat(template.createIssue(baseIssue, event).isPresent()).isFalse();
        Optional<OpenIssueDataCollection> openIssue = getIssueDataCollectionService().findOpenIssue(idcIssue.getId());
        assertThat(openIssue.isPresent()).isTrue();
        assertThat(openIssue.get().getStatus().getKey()).isEqualTo(IssueStatus.OPEN);
    }

    @Test
    @Transactional
    public void testResolveIssue() {
        // Create base issue
        CreationRule rule = getCreationRule("testResolveIssue", ModuleConstants.REASON_UNKNOWN_INBOUND_DEVICE);
        Meter meter = createMeter("1", "mrid");
        Issue baseIssue = getBaseIssue(rule, meter);
        baseIssue.save();
        // Create data-collection issue
        OpenIssueDataCollectionImpl idcIssue = getDataModel().getInstance(OpenIssueDataCollectionImpl.class);
        idcIssue.init(baseIssue);
        idcIssue.setDeviceMRID("1");
        idcIssue.save();
        // Mock event
        IssueEvent event = mock(IssueEvent.class);
        Optional<Issue> issueRef = Optional.of(idcIssue);
        doReturn(issueRef).when(event).findExistingIssue();
        // Test template
        BasicDataCollectionRuleTemplate template = getInjector().getInstance(BasicDataCollectionRuleTemplate.class);
        assertThat(template.resolveIssue(event).isPresent()).isTrue();
    }

    private Meter createMeter(String amrId, String mrid) {
        AmrSystem amrSystem = getMeteringService().findAmrSystem(1).get();
        Meter meter = amrSystem.newMeter(amrId, mrid);
        meter.save();
        return meter;
    }

    private Issue getBaseIssue(CreationRule rule, Meter meter) {
        DataModel isuDataModel = getIssueDataModel();
        Issue baseIssue = isuDataModel.getInstance(OpenIssueImpl.class);
        baseIssue.setStatus(getIssueService().findStatus(IssueStatus.OPEN).get());
        baseIssue.setReason(rule.getReason());
        baseIssue.setDevice(meter);
        baseIssue.setRule(rule);
        return baseIssue;
    }

    private UnknownSlaveDeviceEvent getUnknownDeviceEvent(Long amrId) {
        DeviceService mockDeviceDataService = mock(DeviceService.class);
        TopologyService mockTopologyService = mock(TopologyService.class);
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(amrId);
        when(mockDeviceDataService.findDeviceById(Matchers.anyLong())).thenReturn(Optional.of(device));
        UnknownSlaveDeviceEvent event = new UnknownSlaveDeviceEvent(getIssueDataCollectionService(), getMeteringService(), mockDeviceDataService, mockTopologyService, getCommunicationTaskService(), getThesaurus(), mock(Injector.class));
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put(EventConstants.EVENT_TOPIC, "com/energyict/mdc/inboundcommunication/UNKNOWNDEVICE");
        messageMap.put(ModuleConstants.DEVICE_IDENTIFIER, amrId.toString());
        messageMap.put(EventConstants.TIMESTAMP, Instant.now().toEpochMilli());
        event.wrap(messageMap, DataCollectionEventDescription.UNKNOWN_INBOUND_DEVICE);
        return event;
    }
}
