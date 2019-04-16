/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.task.impl.templates;

import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.issue.task.TaskIssueService;
import com.elster.jupiter.issue.task.OpenTaskIssue;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.tasks.TaskService;

import javax.inject.Inject;

public abstract class AbstractTaskIssueTemplate implements CreationRuleTemplate {

    protected volatile IssueService issueService;
    protected volatile TaskIssueService taskIssueService;
    protected volatile TaskService taskService;
    protected volatile PropertySpecService propertySpecService;
    protected volatile Thesaurus thesaurus;
    
    public AbstractTaskIssueTemplate() {
    }

    @Inject
    protected AbstractTaskIssueTemplate(IssueService issueService, TaskIssueService taskIssueService, TaskService taskService, Thesaurus thesaurus, PropertySpecService propertySpecService) {
        this.issueService = issueService;
        this.taskIssueService = taskIssueService;
        this.taskService = taskService;
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }
    
    Thesaurus getThesaurus() {
        return thesaurus;
    }

    TaskService getTaskService() {
        return taskService;
    }

    @Override
    public OpenTaskIssue createIssue(OpenIssue baseIssue, IssueEvent event) {
        return taskIssueService.createIssue(baseIssue, event);
    }

    @Override
    public IssueType getIssueType() {
        return issueService.findIssueType(TaskIssueService.TASK_ISSUE).get();
    }

    protected void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    protected void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    protected void setTaskIssueService(TaskIssueService taskIssueService) {
        this.taskIssueService = taskIssueService;
    }

    protected void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }
}
