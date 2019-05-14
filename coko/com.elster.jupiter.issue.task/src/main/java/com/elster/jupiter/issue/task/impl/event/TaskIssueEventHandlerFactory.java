/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.task.impl.event;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.issue.task.TaskIssueService;
import com.elster.jupiter.issue.task.impl.ModuleConstants;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.util.json.JsonService;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;

@Component(name = "com.elster.jupiter.issue.task.TaskIssueEventHandlerFactory",
           service = MessageHandlerFactory.class,
           property = {"subscriber=" + ModuleConstants.AQ_TASK_EVENT_SUBSC, "destination=" + EventService.JUPITER_EVENTS},
           immediate = true)
public class TaskIssueEventHandlerFactory implements MessageHandlerFactory {
    private volatile JsonService jsonService;
    private volatile IssueCreationService issueCreationService;
    private volatile IssueService issueService;
    private volatile MeteringService meteringService;
    private volatile TaskService taskService;
    private volatile TaskIssueService taskIssueService;
    private volatile Thesaurus thesaurus;

    // For OSGi framework only
    public TaskIssueEventHandlerFactory() {
        super();
    }

    // For testing purposes only
    @Inject
    public TaskIssueEventHandlerFactory(
            JsonService jsonService,
            IssueService issueService,
            MeteringService meteringService,
            TaskIssueService taskIssueService,
            NlsService nlsService, TaskService taskService) {
        this();
        setJsonService(jsonService);
        setIssueService(issueService);
        setMeteringService(meteringService);
        setTaskIssueService(taskIssueService);
        setTaskService(taskService);
        setNlsService(nlsService);
    }

    @Override
    public MessageHandler newMessageHandler() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(JsonService.class).toInstance(jsonService);
                bind(MeteringService.class).toInstance(meteringService);
                bind(TaskService.class).toInstance(taskService);
                bind(IssueCreationService.class).toInstance(issueCreationService);
                bind(IssueService.class).toInstance(issueService);
                bind(TaskIssueService.class).toInstance(taskIssueService);
            }
        });
        return new TaskIssueEventHandler(injector);
    }

    @Reference
    public final void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public final void setIssueService(IssueService issueService) {
        this.issueService = issueService;
        this.issueCreationService = issueService.getIssueCreationService();
    }

    @Reference
    public final void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(TaskIssueService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public final void setTaskIssueService(TaskIssueService taskIssueService) {
        this.taskIssueService = taskIssueService;
    }

}