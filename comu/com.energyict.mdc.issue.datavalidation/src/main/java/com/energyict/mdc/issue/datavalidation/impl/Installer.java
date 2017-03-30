/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datavalidation.impl;

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
import com.elster.jupiter.upgrade.FullInstaller;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.issue.datavalidation.impl.actions.RetryEstimationAction;
import com.energyict.mdc.issue.datavalidation.impl.event.DataValidationEventDescription;

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

        doTry(
                "Create event subscriber",
                this::setAQSubscriber,
                logger
        );
        doTry(
                "Publish events",
                this::publishEvents,
                logger
        );
    }

    private void createIssueTypeAndReasons(IssueType type) {
        IssueReason failedToEstimateReason = issueService.createReason(IssueDataValidationService.DATA_VALIDATION_ISSUE_REASON, type,
                TranslationKeys.DATA_VALIDATION_ISSUE_REASON, TranslationKeys.DATA_VALIDATION_ISSUE_REASON_DESCRIPTION);
        issueActionService.createActionType(DataValidationActionsFactory.ID, RetryEstimationAction.class.getName(), failedToEstimateReason);
    }

    private void publishEvents() {
        for (DataValidationEventDescription eventDescription : DataValidationEventDescription.values()) {
            eventService.getEventType(eventDescription.getTopic()).ifPresent(eventType -> {
                eventType.setPublish(true);
                eventType.update();
            });
        }
    }
    private IssueType setSupportedIssueType() {
        return issueService.createIssueType(IssueDataValidationService.ISSUE_TYPE_NAME, TranslationKeys.DATA_VALIDATION_ISSUE_TYPE, IssueDataValidationService.DATA_VALIDATION_ISSUE_PREFIX);
    }
    private void setAQSubscriber() {
        DestinationSpec destinationSpec = messageService.getDestinationSpec(EventService.JUPITER_EVENTS).get();
        destinationSpec.subscribe(
                TranslationKeys.AQ_SUBSCRIBER,
                IssueDataValidationService.COMPONENT_NAME,
                Layer.DOMAIN,
                whereCorrelationId()
                        .isEqualTo(DataValidationEventDescription.CANNOT_ESTIMATE_DATA.getTopic())
                        .or(whereCorrelationId().isEqualTo(DataValidationEventDescription.READINGQUALITY_DELETED.getTopic())));
    }
    private void run(Runnable runnable, String explanation, Logger logger) {
        doTry(
                explanation,
                runnable,
                logger
        );
    }
}
