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
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.RecurrentTaskBuilder;
import com.elster.jupiter.tasks.TaskService;

import java.util.logging.Logger;

public class Installer {
    private static final Logger LOG = Logger.getLogger("IssueInstaller");

    private static final String ISSUE_OVERDUE_TASK_NAME = "IssueOverdueTask";
    private static final String ISSUE_OVERDUE_TASK_SCHEDULE = "0 0/1 * 1/1 * ? *";
    private static final int ISSUE_OVERDUE_TASK_RETRY_DELAY = 60;

    private final DataModel dataModel;
    private final IssueService issueService;
    private final IssueActionService issueActionService;
    private final MessageService messageService;
    private final TaskService taskService;

    public Installer(DataModel dataModel, IssueService issueService, MessageService messageService, TaskService taskService) {
        this.dataModel = dataModel;
        this.issueService = issueService;
        this.issueActionService = issueService.getIssueActionService();
        this.messageService = messageService;
        this.taskService = taskService;
    }

    public void install(boolean executeDDL) {
        run(() -> dataModel.install(executeDDL, true), "database schema. Execute command 'ddl " + IssueService.COMPONENT_NAME + "' and apply the sql script manually");
        run(this::createViews, "view for all issues");
        run(this::createStatuses, "default statuses");
        run(this::createIssueOverdueTask, "overdue task");
        run(this::createActionTypes, "action types");
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
        issueActionService.createActionType(IssueDefaultActionsFactory.ID, AssignIssueAction.class.getName(), type);
    }

    public static void run(Runnable runnable, String explanation){
        try {
            runnable.run();
        } catch (Exception stEx){
            LOG.warning("[" + IssueService.COMPONENT_NAME + "] Unable to install " + explanation);
        }
    }

}