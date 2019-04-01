/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.task.impl.install;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.issue.share.entity.CreationRuleActionPhase;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.issue.task.TaskIssueService;
import com.elster.jupiter.issue.task.impl.ModuleConstants;
import com.elster.jupiter.issue.task.impl.TaskIssueActionsFactory;
import com.elster.jupiter.issue.task.impl.actions.CloseIssueAction;
import com.elster.jupiter.issue.task.impl.database.CreateIssueViewOperation;
import com.elster.jupiter.issue.task.impl.event.TaskEventDescription;
import com.elster.jupiter.issue.task.impl.i18n.TranslationKeys;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.DuplicateSubscriberNameException;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import static com.elster.jupiter.messaging.DestinationSpec.whereCorrelationId;

public class Installer implements FullInstaller {
    private static final Logger LOG = Logger.getLogger("TaskIssueInstaller");

    private final MessageService messageService;
    private final IssueService issueService;
    private final IssueActionService issueActionService;
    private final DataModel dataModel;
    private final EventService eventService;

    @Inject
    public Installer(DataModel dataModel, IssueService issueService, IssueActionService issueActionService, MessageService messageService, EventService eventService) {
        this.issueService = issueService;
        this.issueActionService = issueActionService;
        this.messageService = messageService;
        this.dataModel = dataModel;
        this.eventService = eventService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        run(() -> new CreateIssueViewOperation(dataModel).execute(), "database schema. Execute command 'ddl " + TaskIssueService.COMPONENT_NAME + "' and apply the sql script manually", logger);
        run(this::setAQSubscriber, "aq subscribers", logger);
        run(() -> {
            IssueType issueType = setSupportedIssueType();
            setTaskIssueReasons(issueType);
        }, "issue reasons and action types", logger);
        run(this::publishEvents, "event publishing", logger);
        run(this::createEventTypes, "create event types", logger);
    }

    private void publishEvents() {
        Set<EventType> eventTypesToPublish = new HashSet<>();
        for (TaskEventDescription taskEventDescription : TaskEventDescription.values()) {
            eventService.getEventType(taskEventDescription.getTopic()).ifPresent(eventTypesToPublish::add);
        }
        for (EventType eventType : eventTypesToPublish) {
            eventType.setPublish(true);
            eventType.update();
        }
    }

    private IssueType setSupportedIssueType() {
        return issueService.createIssueType(TaskIssueService.TASK_ISSUE, TranslationKeys.ISSUE_TYPE_TASK, TaskIssueService.TASK_ISSUE_PREFIX);
    }

    private void setAQSubscriber() {
        DestinationSpec destinationSpec = messageService.getDestinationSpec(EventService.JUPITER_EVENTS).get();
        try {
            destinationSpec.subscribe(
                    TranslationKeys.AQ_TASK_EVENT_SUBSC,
                    TaskIssueService.COMPONENT_NAME, Layer.DOMAIN,
                    whereCorrelationId()
                            // .like("com/elster/jupiter/tasks/%")
                            //.or(whereCorrelationId().isEqualTo("com/elster/")
                            .isEqualTo("com/elster/jupiter/tasks/taskoccurrence/FAILED"));
        } catch (DuplicateSubscriberNameException e) {
            // subscriber already exists, ignoring
        }
    }


    private void createEventTypes() {
        for (com.elster.jupiter.tasks.EventType eventType : com.elster.jupiter.tasks.EventType.values()) {
            eventType.createIfNotExists(eventService);
        }
    }

    private void setTaskIssueReasons(IssueType issueType) {
        issueService.createReason(ModuleConstants.REASON_TASK_FAILED, issueType,
                TranslationKeys.ISSUE_REASON_TASKFAILED, TranslationKeys.ISSUE_REASON_DESCRIPTION_TASKFAILED);
        issueActionService.createActionType(TaskIssueActionsFactory.ID, CloseIssueAction.class.getName(), issueType, CreationRuleActionPhase.OVERDUE);
    }

    private void run(Runnable runnable, String explanation, Logger logger) {
        doTry(
                explanation,
                runnable,
                logger
        );
    }

}