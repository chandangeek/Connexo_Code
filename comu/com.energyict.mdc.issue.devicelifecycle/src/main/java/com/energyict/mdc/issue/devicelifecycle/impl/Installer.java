/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.devicelifecycle.impl;

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
import com.energyict.mdc.issue.devicelifecycle.IssueDeviceLifecycleService;
import com.energyict.mdc.issue.devicelifecycle.impl.actions.CloseIssueAction;
import com.energyict.mdc.issue.devicelifecycle.impl.actions.RetryTransitionAction;
import com.energyict.mdc.issue.devicelifecycle.impl.event.DeviceLifecycleEventDescription;
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
        IssueReason failedToEstimateReason = issueService.createReason(IssueDeviceLifecycleService.DEVICELIFECYCLE_ISSUE_REASON, type,
                TranslationKeys.DEVICE_LIFECYCLE_ISSUE_REASON, TranslationKeys.DEVICE_LIFECYCLE_ISSUE_REASON_DESCRIPTION);
        issueActionService.createActionType(DeviceLifecycleActionsFactory.ID, RetryTransitionAction.class.getName(), failedToEstimateReason);
        issueActionService.createActionType(DeviceLifecycleActionsFactory.ID, CloseIssueAction.class.getName(), type, CreationRuleActionPhase.OVERDUE);
    }

    private void publishEvents() {
        for (DeviceLifecycleEventDescription eventDescription : DeviceLifecycleEventDescription.values()) {
            eventService.getEventType(eventDescription.getTopic()).ifPresent(eventType -> {
                eventType.setPublish(true);
                eventType.update();
            });
        }
    }

    private IssueType setSupportedIssueType() {
        return issueService.createIssueType(IssueDeviceLifecycleService.ISSUE_TYPE_NAME, TranslationKeys.DEVICE_LIFECYCLE_ISSUE_TYPE, IssueDeviceLifecycleService.DEVICE_LIFECYCLE_ISSUE_PREFIX);
    }

    private void setAQSubscriber() {
        DestinationSpec destinationSpec = messageService.getDestinationSpec(EventService.JUPITER_EVENTS).get();
        destinationSpec.subscribe(
                TranslationKeys.AQ_SUBSCRIBER,
                IssueDeviceLifecycleService.COMPONENT_NAME,
                Layer.DOMAIN,
                whereCorrelationId()
                        .isEqualTo(DeviceLifecycleEventDescription.TRANSITION_FAILURE.getTopic()));
    }
    private void run(Runnable runnable, String explanation, Logger logger) {
        doTry(
                explanation,
                runnable,
                logger
        );
    }
}
