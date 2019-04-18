/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EventType;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.impl.i18n.MessageSeeds;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Listens to 'validate_delete' events of {@link EndPointConfiguration}
 * and will throw an exception for the deletion if it has device alarms
 */
@Component(name = EndPointDeletionDeviceAlarmValidatorEventHandler.NAME, service = TopicHandler.class, immediate = true)
public class EndPointDeletionDeviceAlarmValidatorEventHandler implements TopicHandler {
    static final String NAME = "com.energyict.mdc.device.alarms.EndPointDeletionDeviceAlarmValidatorEventHandler";

    private volatile Thesaurus thesaurus;
    private volatile IssueService issueService;

    // OSGi
    public EndPointDeletionDeviceAlarmValidatorEventHandler() {

    }

    @Override
    public void handle(LocalEvent localEvent) {
        EndPointConfiguration source = (EndPointConfiguration) localEvent.getSource();
        IssueType alarmType = issueService.findIssueType("devicealarm").orElse(null);
        List<IssueReason> alarmReasons = issueService.query(IssueReason.class)
                .select(where("issueType").isEqualTo(alarmType));
        Query<CreationRule> query =
                issueService.getIssueCreationService().getCreationRuleQuery(IssueReason.class, IssueType.class);
        List<CreationRule> rules;
        Condition conditionIssue = where("reason").in(alarmReasons);
        rules = query.select(conditionIssue);
        boolean webServiceIsPresent = rules
                .stream()
                .flatMap(x -> x.getActions().stream())
                .anyMatch(a -> a.getFormattedProperties().equals(source.getName()));

        if (webServiceIsPresent) {
            throw new LocalizedException(this.thesaurus,
                    MessageSeeds.ALARM_RULE_STILL_HAS_ACTIVE_WEB_SERVICE) {
            };
        }
    }

    @Override
    public String getTopicMatcher() {
        return EventType.ENDPOINT_CONFIGURATION_VALIDATE_DELETE.topic();
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public void setThesaurus(DeviceAlarmService deviceAlarmService) {
        this.thesaurus = deviceAlarmService.thesaurus();
    }

}
