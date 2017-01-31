/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.tasks;

import javax.inject.Inject;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.tasks.TaskService;

@Component(name="com.elster.jupiter.issue.tasks.IssueOverdueHandlerFactory",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + IssueOverdueHandlerFactory.ISSUE_OVERDUE_TASK_SUBSCRIBER,
                "destination=" + IssueOverdueHandlerFactory.ISSUE_OVERDUE_TASK_DESTINATION},
        immediate = true)
public class IssueOverdueHandlerFactory implements MessageHandlerFactory{
    public static final String ISSUE_OVERDUE_TASK_DESTINATION = "IssueOverdueTopic";
    public static final String ISSUE_OVERDUE_TASK_SUBSCRIBER = "IssueOverdueSubscriber";
    public static final String ISSUE_OVERDUE_TASK_DISPLAYNAME = "Handle overdue issues";

    private volatile IssueService issueService;
    private volatile TaskService taskService;
    private volatile Thesaurus thesaurus;
    private volatile IssueActionService issueActionService;

    public IssueOverdueHandlerFactory(){}

    @Inject
    public IssueOverdueHandlerFactory(
            TaskService taskService,
            IssueService issueService,
            NlsService nlsService,
            IssueActionService issueActionService) {
        setIssueService(issueService);
        setTaskService(taskService);
        setNlsService(nlsService);
    }

    @Override
    public MessageHandler newMessageHandler() {
        return taskService.createMessageHandler(new IssueOverdueHandler(issueService, thesaurus, issueActionService));
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
}
