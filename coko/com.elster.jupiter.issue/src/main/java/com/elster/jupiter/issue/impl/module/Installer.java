package com.elster.jupiter.issue.impl.module;

import com.elster.jupiter.issue.impl.actions.AssignIssueAction;
import com.elster.jupiter.issue.impl.actions.CloseIssueAction;
import com.elster.jupiter.issue.impl.database.CreateIssueViewOperation;
import com.elster.jupiter.issue.impl.service.IssueDefaultActionsFactory;
import com.elster.jupiter.issue.impl.tasks.IssueOverdueHandlerFactory;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.entity.CreationRuleActionPhase;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.RecurrentTaskBuilder;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.ExceptionCatcher;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Installer {
    private static final String ISSUE_OVERDUE_TASK_NAME = "IssueOverdueTask";
    private static final String ISSUE_OVERDUE_TASK_SCHEDULE = "0 0/1 * 1/1 * ? *";
    private static final int ISSUE_OVERDUE_TASK_RETRY_DELAY = 60;
    private static final Logger LOG = Logger.getLogger("IssueInstaller");

    private final DataModel dataModel;
    private final IssueService issueService;
    private final IssueActionService issueActionService;
    private final MessageService messageService;
    private final UserService userService;
    private final TaskService taskService;

    @Inject
    public Installer(
            DataModel dataModel,
            IssueService issueService,
            IssueActionService issueActionService,
            TaskService taskService,
            MessageService messageService,
            UserService userService) {
        this.dataModel = dataModel;
        this.issueService = issueService;
        this.issueActionService = issueActionService;
        this.messageService = messageService;
        this.taskService = taskService;
        this.userService = userService;
    }

    public void install(boolean executeDDL) {
        ExceptionCatcher
                .executing(
                        () -> dataModel.install(executeDDL, false),
                        this::createViews,
                        this::setDefaultStatuses,
                        this::createPrivileges,
                        this::createIssueOverdueTask,
                        this::createActionTypes
                )
                .andHandleExceptionsWith(e -> LOG.log(Level.SEVERE, e.getMessage(), e))
                .execute();
    }

    private void createViews(){
        new CreateIssueViewOperation(dataModel).execute();
    }

    private void setDefaultStatuses(){
        issueService.createStatus(IssueStatus.OPEN, false, MessageSeeds.ISSUE_STATUS_OPEN);
        issueService.createStatus(IssueStatus.IN_PROGRESS, false, MessageSeeds.ISSUE_STATUS_IN_PROGRESS);
        issueService.createStatus(IssueStatus.RESOLVED, true, MessageSeeds.ISSUE_STATUS_RESOLVED);
        issueService.createStatus(IssueStatus.WONT_FIX, true, MessageSeeds.ISSUE_STATUS_WONT_FIX);
    }

    private void createActionTypes() {
        IssueType type = null;
        issueActionService.createActionType(IssueDefaultActionsFactory.ID, CloseIssueAction.class.getName(), type, CreationRuleActionPhase.OVERDUE);
        issueActionService.createActionType(IssueDefaultActionsFactory.ID, AssignIssueAction.class.getName(), type);
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
}
