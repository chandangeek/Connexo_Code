/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.insight.issue.datavalidation.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.entity.CreationRuleActionPhase;
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
import com.elster.insight.issue.datavalidation.UsagePointIssueDataValidationService;
import com.elster.insight.issue.datavalidation.impl.actions.CloseUsagePointIssueAction;
import com.elster.insight.issue.datavalidation.impl.actions.UsagePointRetryEstimationAction;
import com.elster.insight.issue.datavalidation.impl.event.UsagePointDataValidationEventDescription;
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
                () -> new CreateUsagePointIssueViewOperation(dataModel).execute(),
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
        IssueReason failedToEstimateReason = issueService.createReason(UsagePointIssueDataValidationService.DATA_VALIDATION_ISSUE_REASON, type,
                TranslationKeys.DATA_VALIDATION_ISSUE_REASON, TranslationKeys.DATA_VALIDATION_ISSUE_REASON_DESCRIPTION);
        issueActionService.createActionType(UsagePtDataValidationActionFactory.ID, UsagePointRetryEstimationAction.class.getName(), failedToEstimateReason);
        issueActionService.createActionType(UsagePtDataValidationActionFactory.ID, CloseUsagePointIssueAction.class.getName(), type, CreationRuleActionPhase.OVERDUE);
    }

    private void publishEvents() {
        for (UsagePointDataValidationEventDescription eventDescription : UsagePointDataValidationEventDescription.values()) {
            eventService.getEventType(eventDescription.getTopic()).ifPresent(eventType -> {
                eventType.setPublish(true);
                eventType.update();
            });
        }
    }

    private IssueType setSupportedIssueType() {
        return issueService.createIssueType(UsagePointIssueDataValidationService.ISSUE_TYPE_NAME, TranslationKeys.DATA_VALIDATION_ISSUE_TYPE, UsagePointIssueDataValidationService.DATA_VALIDATION_ISSUE_PREFIX);
    }

    private void setAQSubscriber() {
        DestinationSpec destinationSpec = messageService.getDestinationSpec(EventService.JUPITER_EVENTS).get();
        destinationSpec.subscribe(
                TranslationKeys.AQ_SUBSCRIBER,
                UsagePointIssueDataValidationService.COMPONENT_NAME,
                Layer.DOMAIN,
                whereCorrelationId()
                        .isEqualTo(UsagePointDataValidationEventDescription.CANNOT_ESTIMATE_USAGEPOINT_DATA.getTopic())
                        .or(whereCorrelationId().isEqualTo(UsagePointDataValidationEventDescription.USAGEPOINT_READINGQUALITY_DELETED.getTopic())));
    }
    private void run(Runnable runnable, String explanation, Logger logger) {
        doTry(
                explanation,
                runnable,
                logger
        );
    }
}
