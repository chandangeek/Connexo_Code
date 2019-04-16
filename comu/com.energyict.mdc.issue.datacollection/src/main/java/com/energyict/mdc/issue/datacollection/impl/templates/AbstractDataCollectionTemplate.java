/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.impl.templates;

import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;

import javax.inject.Inject;

public abstract class AbstractDataCollectionTemplate implements CreationRuleTemplate {

    protected volatile IssueService issueService;
    protected volatile IssueDataCollectionService issueDataCollectionService;
    protected volatile PropertySpecService propertySpecService;
    protected volatile DeviceConfigurationService deviceConfigurationService;
    protected volatile DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    protected volatile Thesaurus thesaurus;


    
    public AbstractDataCollectionTemplate() {
    }

    @Inject
    protected AbstractDataCollectionTemplate(IssueService issueService, IssueDataCollectionService issueDataCollectionService, Thesaurus thesaurus, PropertySpecService propertySpecService, DeviceConfigurationService deviceConfigurationService, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.issueService = issueService;
        this.issueDataCollectionService = issueDataCollectionService;
        this.propertySpecService = propertySpecService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
        this.thesaurus = thesaurus;
    }
    
    public Thesaurus getThesaurus() {
        return thesaurus;
    }

    @Override
    public OpenIssueDataCollection createIssue(OpenIssue baseIssue, IssueEvent event) {
        return issueDataCollectionService.createIssue(baseIssue, event);
    }

    @Override
    public IssueType getIssueType() {
        return issueService.findIssueType(IssueDataCollectionService.DATA_COLLECTION_ISSUE).get();
    }

    protected void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    protected void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    protected void setIssueDataCollectionService(IssueDataCollectionService issueDataCollectionService) {
        this.issueDataCollectionService = issueDataCollectionService;
    }

    protected void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    protected void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    protected void setDeviceLifeCycleConfigurationService(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }
}
