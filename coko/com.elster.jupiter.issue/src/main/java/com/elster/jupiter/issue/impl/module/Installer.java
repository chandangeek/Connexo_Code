package com.elster.jupiter.issue.impl.module;

import com.elster.jupiter.issue.impl.actions.AssignIssueAction;
import com.elster.jupiter.issue.impl.database.CreateIssueViewOperation;
import com.elster.jupiter.issue.impl.service.IssueDefaultActionsFactory;
import com.elster.jupiter.issue.impl.tasks.IssueOverdueHandlerFactory;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.util.logging.Logger;

public class Installer implements FullInstaller {

    private static final String ISSUE_OVERDUE_TASK_NAME = "IssueOverdueTask";
    private static final String ISSUE_OVERDUE_TASK_SCHEDULE = "0 0/1 * 1/1 * ? *";
    private static final int ISSUE_OVERDUE_TASK_RETRY_DELAY = 60;

    private final DataModel dataModel;
    private final IssueService issueService;
    private final IssueActionService issueActionService;
    private final MessageService messageService;
    private final TaskService taskService;

    @Inject
    public Installer(DataModel dataModel, IssueService issueService, MessageService messageService, TaskService taskService) {
        this.dataModel = dataModel;
        this.issueService = issueService;
        this.issueActionService = issueService.getIssueActionService();
        this.messageService = messageService;
        this.taskService = taskService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        doTry(
                "view for all issues",
                this::createViews,
                logger
        );
        doTry(
                "default statuses",
                this::createStatuses,
                logger
        );
        doTry(
                "overdue task",
                this::createIssueOverdueTask,
                logger
        );
        doTry(
                "action types",
                this::createActionTypes,
                logger
        );
    }

    private void createViews(){
        new CreateIssueViewOperation(dataModel).execute();
    }

    private void createStatuses(){
        issueService.createStatus(IssueStatus.OPEN, false, TranslationKeys.ISSUE_STATUS_OPEN);
        issueService.createStatus(IssueStatus.IN_PROGRESS, false, TranslationKeys.ISSUE_STATUS_IN_PROGRESS);
        issueService.createStatus(IssueStatus.RESOLVED, true, TranslationKeys.ISSUE_STATUS_RESOLVED);
        issueService.createStatus(IssueStatus.WONT_FIX, true, TranslationKeys.ISSUE_STATUS_WONT_FIX);
    }

    private void createIssueOverdueTask() {
        DestinationSpec destination = messageService.getQueueTableSpec("MSG_RAWTOPICTABLE").get()
                .createDestinationSpec(IssueOverdueHandlerFactory.ISSUE_OVERDUE_TASK_DESTINATION, ISSUE_OVERDUE_TASK_RETRY_DELAY);
        destination.activate();
        destination.subscribe(IssueOverdueHandlerFactory.ISSUE_OVERDUE_TASK_SUBSCRIBER);

        RecurrentTask task = taskService.newBuilder()
                .setApplication("Admin")
                .setName(ISSUE_OVERDUE_TASK_NAME)
                .setScheduleExpressionString(ISSUE_OVERDUE_TASK_SCHEDULE)
                .setDestination(destination)
                .setPayLoad("payload")
                .scheduleImmediately(true)
                .build();
    }

    private void createActionTypes() {
        IssueType type = null;
        issueActionService.createActionType(IssueDefaultActionsFactory.ID, AssignIssueAction.class.getName(), type);
    }

}