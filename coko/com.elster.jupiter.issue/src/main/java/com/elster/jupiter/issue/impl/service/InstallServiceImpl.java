package com.elster.jupiter.issue.impl.service;

import com.elster.jupiter.issue.impl.actions.AssignIssueAction;
import com.elster.jupiter.issue.impl.actions.CloseIssueAction;
import com.elster.jupiter.issue.impl.module.Installer;
import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.impl.tasks.IssueOverdueHandlerFactory;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.entity.CreationRuleActionPhase;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.*;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.*;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.RecurrentTaskBuilder;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.users.UserService;
import com.google.inject.AbstractModule;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;

import java.util.Arrays;
import java.util.List;

@Component(name = "com.elster.jupiter.issue.install", service = {InstallService.class, TranslationKeyProvider.class}, property = "name=" + IssueService.COMPONENT_NAME, immediate = true)
public class InstallServiceImpl implements InstallService, TranslationKeyProvider {
    private static final String ISSUE_OVERDUE_TASK_NAME = "IssueOverdueTask";
    private static final String ISSUE_OVERDUE_TASK_SCHEDULE = "0 0/1 * 1/1 * ? *";
    private static final int ISSUE_OVERDUE_TASK_RETRY_DELAY = 60;

    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile NlsService nlsService;
    private volatile MessageService messageService;
    private volatile MeteringService meteringService;
    private volatile UserService userService;

  
    private volatile TaskService taskService;

    private volatile IssueService issueService;
    private volatile IssueAssignmentService issueAssignmentService;
    private volatile IssueCreationService issueCreationService;
    private volatile IssueActionService issueActionService;

    public InstallServiceImpl() {
    }

    @Inject
    public InstallServiceImpl(
            MessageService messageService,
            MeteringService meteringService,
            TaskService taskService,
            UserService userService,
            IssueService issueService,
            IssueAssignmentService issueAssignmentService,
            IssueCreationService issueCreationService,
            IssueActionService issueActionService,
            IssueMappingService issueMappingService,
            NlsService nlsService) {

        setMessageService(messageService);
        setMeteringService(meteringService);
        setUserService(userService);
        setNlsService(nlsService);

        setTaskService(taskService);

        setIssueService(issueService);
        setIssueAssignmentService(issueAssignmentService);
        setIssueCreationService(issueCreationService);
        setIssueActionService(issueActionService);
        setIssueMappingService(issueMappingService);

        activate();
        if (!dataModel.isInstalled()) {
            install();
        }
    }

    @Activate
    public final void activate() {
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(MessageService.class).toInstance(messageService);
                bind(MeteringService.class).toInstance(meteringService);
                bind(UserService.class).toInstance(userService);

                bind(TaskService.class).toInstance(taskService);

                bind(IssueService.class).toInstance(issueService);
                bind(IssueAssignmentService.class).toInstance(issueAssignmentService);
                bind(IssueCreationService.class).toInstance(issueCreationService);
                bind(IssueActionService.class).toInstance(issueActionService);
                bind(NlsService.class).toInstance(nlsService);
            }
        });
    }

    @Override
    public final void install() {
        new Installer(dataModel, issueService).install(true);

        Installer.run(this::createPrivileges, "privileges");
        Installer.run(this::createIssueOverdueTask, "overdue task");
        Installer.run(this::createActionTypes, "action types");
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("USR", "TSK", "MSG", "ORM", "NLS", "MTR");
    }

    private void createPrivileges() {
        this.userService.createResourceWithPrivileges("MDC", "issue.issues", "issue.issues.description", new String[]{Privileges.VIEW_ISSUE, Privileges.COMMENT_ISSUE, Privileges.CLOSE_ISSUE, Privileges.ASSIGN_ISSUE, Privileges.ACTION_ISSUE});
        this.userService.createResourceWithPrivileges("MDC", "issueConfiguration.issueConfigurations", "issueConfiguration.issueConfigurations.description", new String[]{Privileges.VIEW_CREATION_RULE, Privileges.ADMINISTRATE_CREATION_RULE, Privileges.VIEW_ASSIGNMENT_RULE});
    }

    private void createIssueOverdueTask() {
        DestinationSpec destination = messageService.getQueueTableSpec("MSG_RAWTOPICTABLE").get()
                .createDestinationSpec(IssueOverdueHandlerFactory.ISSUE_OVERDUE_TASK_DESTINATION, ISSUE_OVERDUE_TASK_RETRY_DELAY);
        destination.activate();
        destination.subscribe(IssueOverdueHandlerFactory.ISSUE_OVERDUE_TASK_SUBSCRIBER);

        RecurrentTaskBuilder taskBuilder = taskService.newBuilder();
        taskBuilder.setName(ISSUE_OVERDUE_TASK_NAME);
        taskBuilder.setScheduleExpressionString(ISSUE_OVERDUE_TASK_SCHEDULE);
        taskBuilder.setDestination(destination);
        taskBuilder.setPayLoad("payload");
        taskBuilder.scheduleImmediately();
        RecurrentTask task = taskBuilder.build();
        task.save();
    }

    private void createActionTypes() {
        IssueType type = null;
        issueActionService.createActionType(IssueDefaultActionsFactory.ID, CloseIssueAction.class.getName(), type, CreationRuleActionPhase.OVERDUE);
        issueActionService.createActionType(IssueDefaultActionsFactory.ID, AssignIssueAction.class.getName(), type);
    }

    @Reference
    public final void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public final void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public final void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public final void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public final void setIssueAssignmentService(IssueAssignmentService issueAssignmentService) {
        this.issueAssignmentService = issueAssignmentService;
    }

    @Reference
    public final void setIssueCreationService(IssueCreationService issueCreationService) {
        this.issueCreationService = issueCreationService;
    }

    @Reference
    public final void setIssueMappingService(IssueMappingService issueMappingService) {
        dataModel = IssueMappingServiceImpl.class.cast(issueMappingService).getDataModel();
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(IssueService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public final void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public final void setIssueActionService(IssueActionService issueActionService) {
        this.issueActionService = issueActionService;
    }

    @Override
    public String getComponentName() {
        return IssueService.COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(MessageSeeds.values());
    }
}
