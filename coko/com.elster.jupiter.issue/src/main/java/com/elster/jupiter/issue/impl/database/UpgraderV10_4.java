/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.database;

import com.elster.jupiter.issue.impl.actions.WebServiceNotificationAction;
import com.elster.jupiter.issue.impl.module.TranslationKeys;
import com.elster.jupiter.issue.impl.service.IssueDefaultActionsFactory;
import com.elster.jupiter.issue.impl.tasks.IssueSnoozeHandlerFactory;
import com.elster.jupiter.issue.share.entity.CreationRuleActionPhase;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.util.conditions.Condition;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

import static com.elster.jupiter.orm.Version.version;
import static com.elster.jupiter.util.conditions.Where.where;

@LiteralSql
public class UpgraderV10_4 implements Upgrader {

    private final DataModel dataModel;
    private final IssueService issueService;
    private final IssueActionService issueActionService;
    private final MessageService messageService;
    private final TaskService taskService;

    private final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;
    private static final String ISSUE_SNOOZE_TASK_NAME = "IssueSnoozeTask";
    private static final String ISSUE_SNOOZE_TASK_SCHEDULE = "0 0/1 * 1/1 * ? *";

    @Inject
    UpgraderV10_4(DataModel dataModel, IssueService issueService, IssueActionService issueActionService, MessageService messageService, TaskService taskService) {
        this.dataModel = dataModel;
        this.issueService = issueService;
        this.issueActionService = issueActionService;
        this.messageService = messageService;
        this.taskService = taskService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, version(10, 4));
        this.createStatusesIfNotPresent();
        this.createActionTypesIfNotPresent();
        this.upgradeOpenIssue();
        this.upgradeSubscriberSpecs();
    }

    private void upgradeOpenIssue() {
        try (Connection connection = this.dataModel.getConnection(true)) {
            this.upgradeOpenIssue(connection);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private void upgradeOpenIssue(Connection connection) {
        String[] sqlStatements = {
                "CREATE OR REPLACE VIEW ISU_ISSUE_ALL AS SELECT * FROM ISU_ISSUE_OPEN UNION SELECT * FROM ISU_ISSUE_HISTORY"};
        for (String sqlStatement : sqlStatements) {
            try (PreparedStatement statement = connection.prepareStatement(sqlStatement)) {
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new UnderlyingSQLFailedException(e);
            }
        }
    }


    private void createStatusesIfNotPresent() {
        if (!issueService.findStatus(IssueStatus.SNOOZED).isPresent()) {
            issueService.createStatus(IssueStatus.SNOOZED, false, TranslationKeys.ISSUE_STATUS_SNOOZED);
        }
        if (!issueService.findStatus(IssueStatus.FORWARDED).isPresent()) {
            issueService.createStatus(IssueStatus.FORWARDED, true, TranslationKeys.ISSUE_STATUS_FORWARDED);
        }

    }

    private void upgradeSubscriberSpecs() {
        QueueTableSpec defaultQueueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        this.createMessageHandlerIfNotPresent(defaultQueueTableSpec, IssueSnoozeHandlerFactory.ISSUE_SNOOZE_TASK_DESTINATION, TranslationKeys.ISSUE_SNOOZE_SUBSCRIBER_NAME);
    }

    private void createMessageHandlerIfNotPresent(QueueTableSpec defaultQueueTableSpec, String destinationName, TranslationKey subscriberKey) {
        Optional<DestinationSpec> destinationSpecOptional = messageService.getDestinationSpec(destinationName);
        if (!destinationSpecOptional.isPresent()) {
            DestinationSpec queue = defaultQueueTableSpec.createDestinationSpec(destinationName, DEFAULT_RETRY_DELAY_IN_SECONDS);
            queue.activate();
            queue.subscribe(subscriberKey, IssueService.COMPONENT_NAME, Layer.DOMAIN);
            createTask(ISSUE_SNOOZE_TASK_NAME, ISSUE_SNOOZE_TASK_SCHEDULE, queue );
        } else {
            boolean notSubscribedYet = destinationSpecOptional.get()
                    .getSubscribers()
                    .stream()
                    .noneMatch(spec -> spec.getName().equals(subscriberKey.getKey()));
            if (notSubscribedYet) {
                destinationSpecOptional.get().activate();
                destinationSpecOptional.get().subscribe(subscriberKey, IssueService.COMPONENT_NAME, Layer.DOMAIN);
                createTask(ISSUE_SNOOZE_TASK_NAME, ISSUE_SNOOZE_TASK_SCHEDULE, destinationSpecOptional.get());
            }
        }
    }

    private void createTask(String name, String schedule, DestinationSpec destinationSpec){
        taskService.newBuilder()
                .setApplication("Admin")
                .setName(name)
                .setScheduleExpressionString(schedule)
                .setDestination(destinationSpec)
                .setPayLoad("payload")
                .scheduleImmediately(true)
                .build();
    }

    private void createActionTypesIfNotPresent() {
        IssueType type = null;
        Condition classNameCondition = buildCondition("className", Optional.of(WebServiceNotificationAction.class.getName()));
        Condition factoryCondition = buildCondition("factoryId", Optional.of(IssueDefaultActionsFactory.ID));
        if (issueActionService.getActionTypeQuery().select(classNameCondition.and(factoryCondition)).isEmpty()) {
            issueActionService.createActionType(IssueDefaultActionsFactory.ID, WebServiceNotificationAction.class.getName(), type, CreationRuleActionPhase.CREATE);
        }
    }

    private Condition buildCondition(String field, Optional<?> value) {
        Condition condition = where(field).isNull();
        if (value.isPresent()) {
            condition = condition.or(where(field).isEqualTo(value.get()));
        }
        return condition;
    }

}
