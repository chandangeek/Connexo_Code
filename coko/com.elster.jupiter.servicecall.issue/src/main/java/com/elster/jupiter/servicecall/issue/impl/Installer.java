/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.issue.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.servicecall.issue.IssueServiceCallService;
import com.elster.jupiter.servicecall.issue.ModuleConstants;
import com.elster.jupiter.servicecall.issue.ServiceCallActionsFactory;
import com.elster.jupiter.servicecall.issue.TranslationKeys;
import com.elster.jupiter.servicecall.issue.impl.action.FailedAction;
import com.elster.jupiter.servicecall.issue.impl.action.PartialSucceedAction;
import com.elster.jupiter.servicecall.issue.impl.event.ServiceCallEventDescription;
import com.elster.jupiter.upgrade.FullInstaller;

import com.google.inject.Inject;

import java.util.logging.Logger;

import static com.elster.jupiter.messaging.DestinationSpec.whereCorrelationId;

class Installer implements FullInstaller {

    private final IssueService issueService;
    private final IssueActionService issueActionService;
    private final DataModel dataModel;
    private final EventService eventService;
    private final MessageService messageService;

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
        doTry(
                "Create issue view operation",
                () -> new CreateIssueViewOperation(dataModel).execute(),
                logger
        );
        run(() -> {
            IssueType issueType = setSupportedIssueType();
            createIssueTypeAndReasons(issueType);
        }, "issue reasons and action types", logger);

//        doTry(
//                "Create event subscriber",
//                this::setAQSubscriber,
//                logger
//        );
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
        return issueService.createIssueType(IssueServiceCallService.ISSUE_TYPE_NAME, TranslationKeys.SERVICE_CALL_ISSUE_TYPE, IssueServiceCallService.SERVICE_CALL_ISSUE_PREFIX);
    }

    private void setAQSubscriber() {
        DestinationSpec destinationSpec = messageService.getDestinationSpec(EventService.JUPITER_EVENTS).get();
        destinationSpec.subscribe(
                TranslationKeys.AQ_SUBSCRIBER,
                IssueServiceCallService.COMPONENT_NAME,
                Layer.DOMAIN,
                whereCorrelationId()
                        .isEqualTo(ServiceCallEventDescription.CANNOT_ESTIMATE_DATA.getTopic())
                        .or(whereCorrelationId().isEqualTo(ServiceCallEventDescription.CANNOT_ESTIMATE_DATA.getTopic())));
    }

    private void createIssueTypeAndReasons(IssueType issueType) {
        IssueReason serviceCallFailed = issueService.createReason(ModuleConstants.REASON_FAILED, issueType,
                TranslationKeys.SERVICE_CALL_ISSUE_FAILED_REASON, TranslationKeys.SERVICE_CALL_ISSUE_FAILED_REASON_DESCRIPTION);
        IssueReason serviceCallPartialSucceed = issueService.createReason(ModuleConstants.REASON_PARTIAL_SUCCEED, issueType,
                TranslationKeys.SERVICE_CALL_ISSUE_PARTIAL_SUCCEED_REASON, TranslationKeys.SERVICE_CALL_ISSUE_PARTIAL_SUCCEED_REASON_DESCRIPTION);

        issueActionService.createActionType(ServiceCallActionsFactory.ID, FailedAction.class.getName(), serviceCallFailed);
        issueActionService.createActionType(ServiceCallActionsFactory.ID, PartialSucceedAction.class.getName(), serviceCallPartialSucceed);
    }

    private void run(Runnable runnable, String explanation, Logger logger) {
        doTry(
                explanation,
                runnable,
                logger
        );
    }
}
