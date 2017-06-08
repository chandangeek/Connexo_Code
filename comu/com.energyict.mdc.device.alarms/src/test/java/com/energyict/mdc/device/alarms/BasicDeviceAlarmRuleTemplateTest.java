/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.issue.impl.service.IssueServiceImpl;
import com.elster.jupiter.issue.share.IssueProvider;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.metering.Meter;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.energyict.mdc.device.alarms.entity.OpenDeviceAlarm;
import com.energyict.mdc.device.alarms.event.EndDeviceEventCreatedEvent;
import com.energyict.mdc.device.alarms.impl.ModuleConstants;
import com.energyict.mdc.device.alarms.impl.records.HistoricalDeviceAlarmImpl;
import com.energyict.mdc.device.alarms.impl.records.OpenDeviceAlarmImpl;
import com.energyict.mdc.device.alarms.impl.templates.BasicDeviceAlarmRuleTemplate;

import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BasicDeviceAlarmRuleTemplateTest extends BaseTest {

    @Test
    @Transactional
    public void testCanCreateAlarm() {
        CreationRule rule = getCreationRule("testCanCreateAlarm");
        Meter meter = createMeter("1", "Name");
        OpenIssue baseIssue = createBaseIssue(rule, meter);

        BasicDeviceAlarmRuleTemplate template = getInjector().getInstance(BasicDeviceAlarmRuleTemplate.class);
        EndDeviceEventCreatedEvent event = getEndDeviceEventCreatedEvent(1L);

        OpenDeviceAlarm alarm = template.createIssue(baseIssue, event);
        assertThat(alarm.getId()).isEqualTo(baseIssue.getId());
        assertThat(alarm.getDevice().getAmrId()).isEqualTo("1");
    }

    @Test
    @Transactional
    public void testCanCreateAlarmOnAnotherDevice() {
        CreationRule rule = getCreationRule("testCanCreateAlarmOnAnotherDevice");
        Meter meter = createMeter("1", "Name1");
        OpenIssue baseIssue = createBaseIssue(rule, meter);
        OpenDeviceAlarmImpl alarm = getDataModel().getInstance(OpenDeviceAlarmImpl.class);
        alarm.setIssue(baseIssue);
        alarm.save();

        meter = createMeter("2", "Name2");
        baseIssue = createBaseIssue(rule, meter);

        BasicDeviceAlarmRuleTemplate template = getInjector().getInstance(BasicDeviceAlarmRuleTemplate.class);
        EndDeviceEventCreatedEvent event = getEndDeviceEventCreatedEvent(2L);

        OpenDeviceAlarm newAlarm = template.createIssue(baseIssue, event);
        assertThat(newAlarm.getId()).isEqualTo(baseIssue.getId());
        assertThat(newAlarm.getDevice().getAmrId()).isEqualTo("2");
    }

    @Test
    @Transactional
    public void testInProgressToOpenTransition() {
        CreationRule rule = getCreationRule("testInProgressToOpenTransition");
        Meter meter = createMeter("1", "Name");
        OpenIssue baseIssue = createBaseIssue(rule, meter);
        OpenDeviceAlarmImpl alarm = getDataModel().getInstance(OpenDeviceAlarmImpl.class);
        alarm.setIssue(baseIssue);
        alarm.setStatus(getIssueService().findStatus(IssueStatus.IN_PROGRESS).get());
        alarm.save();

        BasicDeviceAlarmRuleTemplate template = getInjector().getInstance(BasicDeviceAlarmRuleTemplate.class);
        EndDeviceEventCreatedEvent event = getEndDeviceEventCreatedEvent(1L);
        template.updateIssue(alarm, event);

        Optional<OpenDeviceAlarm> openIssue = getDeviceAlarmService().findOpenAlarm(alarm.getId());
        assertThat(openIssue.isPresent()).isTrue();
        assertThat(openIssue.get().getStatus().getKey()).isEqualTo(IssueStatus.OPEN);
    }

    @Test
    @Transactional
    public void testCloseAlarm() {
        ((IssueServiceImpl) getIssueService()).addIssueProvider((IssueProvider) getDeviceAlarmService());

        CreationRule rule = getCreationRule("testCloseAlarmBaseIssue");
        Meter meter = createMeter("1", "Name");
        EndDeviceEventCreatedEvent event = getEndDeviceEventCreatedEvent(1L);
        DeviceAlarm alarm = getDeviceAlarmService().createAlarm(createBaseIssue(rule, meter), event);
        assertThat(alarm instanceof OpenDeviceAlarmImpl).isTrue();
        ((OpenIssue) alarm).close(getIssueService().findStatus(IssueStatus.WONT_FIX).get());
        Optional<? extends DeviceAlarm> closedAlarm = getDeviceAlarmService().findAlarm(alarm.getId());
        assertThat(closedAlarm.get() instanceof HistoricalDeviceAlarmImpl).isTrue();
        assertThat(closedAlarm.get().getStatus().getKey()).isEqualTo(IssueStatus.WONT_FIX);
    }

}
