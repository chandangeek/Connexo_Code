/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.tasks;

import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.tasks.TaskService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;

@Component(name="com.elster.jupiter.issue.tasks.IssueSnoozeHandlerFactory",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + IssueSnoozeHandlerFactory.ISSUE_SNOOZE_TASK_SUBSCRIBER,
                "destination=" + IssueSnoozeHandlerFactory.ISSUE_SNOOZE_TASK_DESTINATION},
        immediate = true)
public class IssueSnoozeHandlerFactory implements MessageHandlerFactory{
    public static final String ISSUE_SNOOZE_TASK_DESTINATION = "IssueSnoozeTopic";
    public static final String ISSUE_SNOOZE_TASK_SUBSCRIBER = "IssueSnoozeSubscriber";
    public static final String ISSUE_SNOOZE_TASK_DISPLAYNAME = "Handle snoozed issues";

    private volatile IssueService issueService;
    private volatile TaskService taskService;
    private volatile Thesaurus thesaurus;
    private volatile IssueActionService issueActionService;
    private volatile Clock clock;

    public IssueSnoozeHandlerFactory(){}

    @Inject
    public IssueSnoozeHandlerFactory(
            TaskService taskService,
            IssueService issueService,
            NlsService nlsService) {
        setIssueService(issueService);
        setTaskService(taskService);
        setNlsService(nlsService);
    }

    @Override
    public MessageHandler newMessageHandler() {
        return taskService.createMessageHandler(new IssueSnoozeHandler(issueService, thesaurus, issueActionService, clock));
    }

    @Reference
    public final void setIssueService(IssueService issueService) {
        this.issueService = issueService;
        this.issueActionService = issueService.getIssueActionService();
    }

    @Reference
    public final void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(IssueService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public final void setClock(Clock clock) {
        this.clock = clock;
    }

}
