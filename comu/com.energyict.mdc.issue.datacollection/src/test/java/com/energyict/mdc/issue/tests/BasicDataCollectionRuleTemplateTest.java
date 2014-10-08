package com.energyict.mdc.issue.tests;

import com.elster.jupiter.issue.impl.records.OpenIssueImpl;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.transaction.TransactionContext;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.issue.datacollection.event.UnknownInboundDeviceEvent;
import com.energyict.mdc.issue.datacollection.impl.ModuleConstants;
import com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription;
import com.energyict.mdc.issue.datacollection.impl.records.OpenIssueDataCollectionImpl;
import com.energyict.mdc.issue.datacollection.impl.templates.BasicDatacollectionRuleTemplate;
import org.junit.Test;
import org.mockito.Matchers;
import org.osgi.service.event.EventConstants;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BasicDataCollectionRuleTemplateTest extends BaseTest {

    @Test
    public void testCanCreateIssue(){
        try (TransactionContext context = getContext()){
            CreationRule rule = getCreationRule(ModuleConstants.REASON_CONNECTION_FAILED);
            Meter meter = createMeter("1", "mrid");
            Issue baseIssue = getBaseIssue(rule, meter);

            BasicDatacollectionRuleTemplate template = getInjector().getInstance(BasicDatacollectionRuleTemplate.class);
            UnknownInboundDeviceEvent event = getUnknownInboundDeviceEvent(1L);

            assertThat(template.createIssue(baseIssue, event).isPresent()).isTrue();
        }
    }

    @Test
    public void testCanCreateIssueOnAnotherDevice(){
        try (TransactionContext context = getContext()){
            CreationRule rule = getCreationRule(ModuleConstants.REASON_CONNECTION_FAILED);
            Meter meter = createMeter("1", "mrid");
            Issue baseIssue = getBaseIssue(rule, meter);
            baseIssue.save();
            OpenIssueDataCollectionImpl idcIssue = getDataModel().getInstance(OpenIssueDataCollectionImpl.class);
            idcIssue.init(baseIssue);
            idcIssue.setDeviceSerialNumber("001234");
            idcIssue.save();

            meter = createMeter("2", "mrid2");
            baseIssue = getBaseIssue(rule, meter);

            BasicDatacollectionRuleTemplate template = getInjector().getInstance(BasicDatacollectionRuleTemplate.class);
            UnknownInboundDeviceEvent event = getUnknownInboundDeviceEvent(2L);

            assertThat(template.createIssue(baseIssue, event).isPresent()).isTrue();
        }
    }

    @Test
    public void testCanNotCreateIssue(){
        try (TransactionContext context = getContext()){
            CreationRule rule = getCreationRule(ModuleConstants.REASON_UNKNOWN_INBOUND_DEVICE);
            Meter meter = createMeter("1", "mrid");
            Issue baseIssue = getBaseIssue(rule, meter);
            baseIssue.save();
            OpenIssueDataCollectionImpl idcIssue = getDataModel().getInstance(OpenIssueDataCollectionImpl.class);
            idcIssue.init(baseIssue);
            idcIssue.setDeviceSerialNumber("001234");
            idcIssue.save();
            baseIssue = getBaseIssue(rule, meter);
            BasicDatacollectionRuleTemplate template = getInjector().getInstance(BasicDatacollectionRuleTemplate.class);
            UnknownInboundDeviceEvent event = getUnknownInboundDeviceEvent(1L);
            assertThat(template.createIssue(baseIssue, event).isPresent()).isFalse();
        }
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

    private UnknownInboundDeviceEvent getUnknownInboundDeviceEvent(Long amrId) {
        DeviceService mockDeviceDataService = mock(DeviceService.class);
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(amrId);
        when(mockDeviceDataService.findDeviceById(Matchers.anyLong())).thenReturn(device);
        UnknownInboundDeviceEvent event = new UnknownInboundDeviceEvent(getIssueDataCollectionService(), getIssueService(), getMeteringService(), mockDeviceDataService, getCommunicationTaskService(), getThesaurus());
        Map messageMap = new HashMap<>();
        messageMap.put(EventConstants.EVENT_TOPIC, "com/energyict/mdc/inboundcommunication/UNKNOWNDEVICE");
        messageMap.put(ModuleConstants.DEVICE_IDENTIFIER, amrId.toString());
        event.wrap(messageMap, DataCollectionEventDescription.UNKNOWN_INBOUND_DEVICE);
        return event;
    }
}
