/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.impl.templates;

import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.entity.OpenDeviceAlarm;

import javax.inject.Inject;

public abstract class AbstractDeviceAlarmTemplate implements CreationRuleTemplate {

    protected final IssueService issueService;
    protected final DeviceAlarmService deviceAlarmService;
    protected final PropertySpecService propertySpecService;
    protected final Thesaurus thesaurus;
    
    @Inject
    protected AbstractDeviceAlarmTemplate(IssueService issueService, DeviceAlarmService deviceAlarmService, Thesaurus thesaurus, PropertySpecService propertySpecService) {
        this.issueService = issueService;
        this.deviceAlarmService = deviceAlarmService;
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }
    
    public Thesaurus getThesaurus() {
        return thesaurus;
    }

    @Override
    public OpenDeviceAlarm createIssue(OpenIssue baseIssue, IssueEvent event) {
        return deviceAlarmService.createAlarm(baseIssue, event);
    }

    @Override
    public IssueType getIssueType() {
        return issueService.findIssueType(DeviceAlarmService.DEVICE_ALARM).get();
    }
}
