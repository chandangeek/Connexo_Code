/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.issue.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.entity.CreationRuleActionPhase;
import com.elster.jupiter.issue.share.entity.IssueReason;
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
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.servicecall.issue.ServiceCallIssueService;
import com.elster.jupiter.servicecall.issue.ModuleConstants;
import com.elster.jupiter.servicecall.issue.impl.action.FailedAction;
import com.elster.jupiter.servicecall.issue.impl.action.PartialSucceedAction;
import com.elster.jupiter.servicecall.issue.impl.action.StartProcessAction;
import com.elster.jupiter.servicecall.issue.impl.event.ServiceCallEventDescription;
import com.elster.jupiter.servicecall.issue.impl.i18n.TranslationKeys;
import com.elster.jupiter.upgrade.FullInstaller;

import com.google.inject.Inject;

import java.util.Optional;
import java.util.logging.Logger;

class Installer implements FullInstaller {

    private final IssueService issueService;
    private final IssueActionService issueActionService;
    private final DataModel dataModel;
    private final EventService eventService;
    private final MessageService messageService;

    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;

    @Inject
    Installer(DataModel dataModel, IssueService issueService, IssueActionService issueActionService, EventService eventService, MessageService messageService) {
        this.dataModel = dataModel;
        this.issueService = issueService;
        this.issueActionService = issueActionService;
        this.eventService = eventService;
        this.messageService = messageService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        QueueTableSpec defaultQueueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        createMessageHandler(defaultQueueTableSpec, ServiceCallIssueServiceImpl.ISSUE_SERVICE_CALLS_DESTINATION_NAME, TranslationKeys.ISSUE_SERVICE_CALL_SUBSCRIBER, logger);
        doTry(
                "Create issue view operation",
                () -> new CreateIssueViewOperation(dataModel).execute(),
                logger
        );
        run(() -> {
            IssueType issueType = setSupportedIssueType();
            createIssueTypeAndReasons(issueType);
        }, "issue reasons and action types", logger);

        doTry(
                "Publish events",
                this::publishEvents,
                logger
        );
    }

    private void publishEvents() {
        for (ServiceCallEventDescription eventDescription : ServiceCallEventDescription.values()) {
            eventService.getEventType(eventDescription.getTopic()).ifPresent(eventType -> {
                eventType.setPublish(true);
                eventType.update();
            });
        }
    }

    private IssueType setSupportedIssueType() {
        return issueService.createIssueType(ServiceCallIssueService.ISSUE_TYPE_NAME, TranslationKeys.SERVICE_CALL_ISSUE_TYPE, ServiceCallIssueService.SERVICE_CALL_ISSUE_PREFIX);
    }

    private void createIssueTypeAndReasons(IssueType issueType) {
        IssueReason serviceCallFailed = issueService.createReason(ModuleConstants.REASON_FAILED, issueType,
                TranslationKeys.SERVICE_CALL_ISSUE_FAILED_REASON, TranslationKeys.SERVICE_CALL_ISSUE_FAILED_REASON_DESCRIPTION);
        IssueReason serviceCallPartialSucceed = issueService.createReason(ModuleConstants.REASON_PARTIAL_SUCCEED, issueType,
                TranslationKeys.SERVICE_CALL_ISSUE_PARTIAL_SUCCEED_REASON, TranslationKeys.SERVICE_CALL_ISSUE_PARTIAL_SUCCEED_REASON_DESCRIPTION);

        issueActionService.createActionType(ServiceCallIssueActionsFactory.ID, FailedAction.class.getName(), serviceCallFailed);
        issueActionService.createActionType(ServiceCallIssueActionsFactory.ID, PartialSucceedAction.class.getName(), serviceCallPartialSucceed);
        issueActionService.createActionType(ServiceCallIssueActionsFactory.ID, StartProcessAction.class.getName(), issueType, CreationRuleActionPhase.CREATE);
    }

    private void run(Runnable runnable, String explanation, Logger logger) {
        doTry(
                explanation,
                runnable,
                logger
        );
    }

    private void createMessageHandler(QueueTableSpec defaultQueueTableSpec, String destinationName, TranslationKey subscriberName, Logger logger) {
        Optional<DestinationSpec> destinationSpecOptional = messageService.getDestinationSpec(destinationName);
        if (!destinationSpecOptional.isPresent()) {
            DestinationSpec queue = doTry(
                    "Create Queue : " + ServiceCallIssueServiceImpl.ISSUE_SERVICE_CALLS_DESTINATION_NAME,
                    () -> {
                        DestinationSpec destinationSpec = defaultQueueTableSpec.createDestinationSpec(destinationName, DEFAULT_RETRY_DELAY_IN_SECONDS);
                        destinationSpec.activate();
                        return destinationSpec;
                    },
                    logger
            );
            doTry(
                    "Create subsriber " + ServiceCallIssueServiceImpl.ISSUE_SERVICE_CALLS_SUBSCRIBER_NAME + " on " + ServiceCallIssueServiceImpl.ISSUE_SERVICE_CALLS_DESTINATION_NAME,
                    () -> queue.subscribe(TranslationKeys.ISSUE_SERVICE_CALL_SUBSCRIBER, ServiceCallIssueService.COMPONENT_NAME, Layer.DOMAIN),
                    logger
            );
        } else {
            DestinationSpec queue = destinationSpecOptional.get();
            boolean notSubscribedYet = queue
                    .getSubscribers()
                    .stream()
                    .noneMatch(spec -> spec.getName().equals(subscriberName.getKey()));
            if (notSubscribedYet) {
                doTry(
                        "Create subsriber " + ServiceCallIssueServiceImpl.ISSUE_SERVICE_CALLS_SUBSCRIBER_NAME + " on " + ServiceCallIssueServiceImpl.ISSUE_SERVICE_CALLS_DESTINATION_NAME,
                        () -> {
                            queue.activate();
                            queue.subscribe(subscriberName, ServiceCallIssueService.COMPONENT_NAME, Layer.DOMAIN);
                        },
                        logger
                );
            }
        }

    }

}
