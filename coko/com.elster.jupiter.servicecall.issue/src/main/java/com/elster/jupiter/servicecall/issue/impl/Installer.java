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
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.servicecall.issue.ModuleConstants;
import com.elster.jupiter.servicecall.issue.ServiceCallIssueService;
import com.elster.jupiter.servicecall.issue.impl.action.FailedAction;
import com.elster.jupiter.servicecall.issue.impl.action.PartialSucceedAction;
import com.elster.jupiter.servicecall.issue.impl.action.StartProcessAction;
import com.elster.jupiter.servicecall.issue.impl.event.ServiceCallEventDescription;
import com.elster.jupiter.servicecall.issue.impl.i18n.TranslationKeys;
import com.elster.jupiter.upgrade.FullInstaller;

import com.google.inject.Inject;

import java.util.logging.Logger;

class Installer implements FullInstaller {

    private final IssueService issueService;
    private final IssueActionService issueActionService;
    private final DataModel dataModel;
    private final EventService eventService;

    @Inject
    Installer(DataModel dataModel, IssueService issueService, IssueActionService issueActionService, EventService eventService) {
        this.dataModel = dataModel;
        this.issueService = issueService;
        this.issueActionService = issueActionService;
        this.eventService = eventService;
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
}
