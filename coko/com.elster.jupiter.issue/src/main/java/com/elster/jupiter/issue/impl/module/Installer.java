/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.module;

import com.elster.jupiter.issue.impl.actions.AssignIssueAction;
import com.elster.jupiter.issue.impl.actions.WebServiceNotificationAction;
import com.elster.jupiter.issue.impl.database.CreateIssueViewOperation;
import com.elster.jupiter.issue.impl.service.IssueDefaultActionsFactory;
import com.elster.jupiter.issue.impl.tasks.IssueOverdueHandlerFactory;
import com.elster.jupiter.issue.impl.tasks.IssueSnoozeHandlerFactory;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.entity.CreationRuleActionPhase;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class Installer implements FullInstaller, PrivilegesProvider {

    private static final String ISSUE_OVERDUE_TASK_NAME = "IssueOverdueTask";
    private static final String ISSUE_OVERDUE_TASK_SCHEDULE = "0 0/1 * 1/1 * ? *";
    private static final int ISSUE_OVERDUE_TASK_RETRY_DELAY = 60;

    private static final String ISSUE_SNOOZE_TASK_NAME = "IssueSnoozeTask";
    private static final String ISSUE_SNOOZE_TASK_SCHEDULE = "0 0/1 * 1/1 * ? *";
    private static final int ISSUE_SNOOZE_TASK_RETRY_DELAY = 60;

    private final DataModel dataModel;
    private final IssueService issueService;
    private final IssueActionService issueActionService;
    private final MessageService messageService;
    private final TaskService taskService;
    private final UserService userService;
    private final EndPointConfigurationService endPointConfigurationService;

    @Inject
    public Installer(DataModel dataModel, IssueService issueService, MessageService messageService, TaskService taskService, UserService userService, EndPointConfigurationService endPointConfigurationService) {
        this.dataModel = dataModel;
        this.issueService = issueService;
        this.userService = userService;
        this.issueActionService = issueService.getIssueActionService();
        this.messageService = messageService;
        this.taskService = taskService;
        this.endPointConfigurationService = endPointConfigurationService;
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

                "snooze task",
                this::createIssueSnoozeTask,
                logger
        );
        doTry(
                "action types",
                this::createActionTypes,
                logger
        );
        userService.addModulePrivileges(this);
    }

    @Override
    public String getModuleName() {
        return IssueService.COMPONENT_NAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(IssueService.COMPONENT_NAME, Privileges.RESOURCE_ISSUES.getKey(), Privileges.RESOURCE_ISSUES_DESCRIPTION.getKey(),
                Arrays.asList(
                        Privileges.Constants.VIEW_ISSUE, Privileges.Constants.COMMENT_ISSUE,
                        Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.ASSIGN_ISSUE,
                        Privileges.Constants.ACTION_ISSUE
                )));
        resources.add(userService.createModuleResourceWithPrivileges(IssueService.COMPONENT_NAME, Privileges.RESOURCE_ISSUES_CONFIGURATION.getKey(), Privileges.RESOURCE_ISSUES_CONFIGURATION_DESCRIPTION
                        .getKey(),
                Arrays.asList(
                        Privileges.Constants.VIEW_CREATION_RULE,
                        Privileges.Constants.ADMINISTRATE_CREATION_RULE, Privileges.Constants.VIEW_ASSIGNMENT_RULE
                )));
        return resources;
    }

    private void createViews() {
        new CreateIssueViewOperation(dataModel).execute();
    }

    private void createStatuses() {
        issueService.createStatus(IssueStatus.OPEN, false, TranslationKeys.ISSUE_STATUS_OPEN);
        issueService.createStatus(IssueStatus.FORWARDED, true, TranslationKeys.ISSUE_STATUS_FORWARDED);
        issueService.createStatus(IssueStatus.IN_PROGRESS, false, TranslationKeys.ISSUE_STATUS_IN_PROGRESS);
        issueService.createStatus(IssueStatus.RESOLVED, true, TranslationKeys.ISSUE_STATUS_RESOLVED);
        issueService.createStatus(IssueStatus.SNOOZED, false, TranslationKeys.ISSUE_STATUS_SNOOZED);
        issueService.createStatus(IssueStatus.WONT_FIX, true, TranslationKeys.ISSUE_STATUS_WONT_FIX);
    }

    private void createIssueOverdueTask() {

        createActionTask(IssueOverdueHandlerFactory.ISSUE_OVERDUE_TASK_DESTINATION,
                ISSUE_OVERDUE_TASK_RETRY_DELAY,
                TranslationKeys.SUBSCRIBER_NAME,
                ISSUE_OVERDUE_TASK_NAME,
                ISSUE_OVERDUE_TASK_SCHEDULE);
    }

    private void createIssueSnoozeTask() {
        createActionTask(IssueSnoozeHandlerFactory.ISSUE_SNOOZE_TASK_DESTINATION,
                ISSUE_SNOOZE_TASK_RETRY_DELAY,
                TranslationKeys.ISSUE_SNOOZE_SUBSCRIBER_NAME,
                ISSUE_SNOOZE_TASK_NAME,
                ISSUE_SNOOZE_TASK_SCHEDULE);
    }


    private void createActionTask(String destinationSpecName, int destinationSpecRetryDelay, TranslationKey subscriberSpecName, String taskName, String taskSchedule) {
        DestinationSpec destination = messageService.getQueueTableSpec("MSG_RAWTOPICTABLE").get()
                .createDestinationSpec(destinationSpecName, destinationSpecRetryDelay);
        destination.activate();
        destination.subscribe(subscriberSpecName, IssueService.COMPONENT_NAME, Layer.DOMAIN);

        taskService.newBuilder()
                .setApplication("Admin")
                .setName(taskName)
                .setScheduleExpressionString(taskSchedule)
                .setDestination(destination)
                .setPayLoad("payload")
                .scheduleImmediately(true)
                .build();
    }

    private void createActionTypes() {
        IssueType type = null;
        issueActionService.createActionType(IssueDefaultActionsFactory.ID, AssignIssueAction.class.getName(), type);
        issueActionService.createActionType(IssueDefaultActionsFactory.ID, WebServiceNotificationAction.class.getName(), type, CreationRuleActionPhase.CREATE);
    }

}