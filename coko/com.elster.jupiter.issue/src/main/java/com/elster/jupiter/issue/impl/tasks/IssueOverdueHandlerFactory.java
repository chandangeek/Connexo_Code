package com.elster.jupiter.issue.impl.tasks;

import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.util.json.JsonService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name="com.elster.jupiter.issue.tasks.IssueOverdueHandlerFactory", service = MessageHandlerFactory.class, property = {"subscriber=" + IssueOverdueHandlerFactory.ISSUE_OVERDUE_TASK_SUBSCRIBER, "destination=" + IssueOverdueHandlerFactory.ISSUE_OVERDUE_TASK_DESTINATION}, immediate = true)
public class IssueOverdueHandlerFactory implements MessageHandlerFactory{
    public static final String ISSUE_OVERDUE_TASK_DESTINATION = "IssueOverdueTopic";
    public static final String ISSUE_OVERDUE_TASK_SUBSCRIBER = "IssueOverdueSubscriber";

    private volatile JsonService jsonService;
    private volatile IssueService issueService;
    private volatile MeteringService meteringService;
    private volatile TaskService taskService;
    private volatile Thesaurus thesaurus;
    private volatile IssueActionService issueActionService;

    public IssueOverdueHandlerFactory(){}

    @Inject
    public IssueOverdueHandlerFactory(
            JsonService jsonService,
            TaskService taskService,
            IssueService issueService,
            NlsService nlsService,
            MeteringService meteringService,
            IssueActionService issueActionService) {
        setJsonService(jsonService);
        setMeteringService(meteringService);
        setIssueService(issueService);
        setTaskService(taskService);
        setNlsService(nlsService);
        setIssueActionService(issueActionService);
    }

    @Override
    public MessageHandler newMessageHandler() {
        return taskService.createMessageHandler(new IssueOverdueHandler(issueService, thesaurus, issueActionService));
    }

    @Reference
    public final void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }
    @Reference
    public final void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }
    @Reference
    public final void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
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
    public final void setIssueActionService(IssueActionService issueActionService) {
        this.issueActionService = issueActionService;
    }
}
