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
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.entity.OpenDeviceAlarm;
import com.energyict.mdc.dynamic.PropertySpecService;

import javax.inject.Inject;

public abstract class AbstractDeviceAlarmTemplate implements CreationRuleTemplate {

    protected volatile IssueService issueService;
    protected volatile DeviceAlarmService deviceAlarmService;
    protected volatile PropertySpecService propertySpecService;
    protected volatile Thesaurus thesaurus;
    
    public AbstractDeviceAlarmTemplate() {
    }

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

    protected void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    protected void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    protected void setDeviceAlarmService(DeviceAlarmService deviceAlarmService) {
        this.deviceAlarmService = deviceAlarmService;
    }

    protected void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }
}
