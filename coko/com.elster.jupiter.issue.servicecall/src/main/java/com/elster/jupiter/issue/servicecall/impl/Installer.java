/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.servicecall.impl;

import com.elster.jupiter.issue.servicecall.ModuleConstants;
import com.elster.jupiter.issue.servicecall.ServiceCallIssueService;
import com.elster.jupiter.issue.servicecall.impl.action.FailedAction;
import com.elster.jupiter.issue.servicecall.impl.action.PartialSucceedAction;
import com.elster.jupiter.issue.servicecall.impl.action.RetryServiceCallAction;
import com.elster.jupiter.issue.servicecall.impl.action.StartProcessAction;
import com.elster.jupiter.issue.servicecall.impl.i18n.TranslationKeys;
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

import com.google.inject.Inject;

import java.util.logging.Logger;

import static com.elster.jupiter.servicecall.ServiceCallService.SERVICE_CALLS_DESTINATION_NAME;

class Installer implements FullInstaller {

    private final IssueService issueService;
    private final IssueActionService issueActionService;
    private final DataModel dataModel;
    private final MessageService messageService;

    @Inject
    Installer(DataModel dataModel, IssueService issueService, IssueActionService issueActionService, MessageService messageService) {
        this.dataModel = dataModel;
        this.issueService = issueService;
        this.issueActionService = issueActionService;
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
    }


    private void setAQSubscriber() {
        DestinationSpec destinationSpec = messageService.getDestinationSpec(SERVICE_CALLS_DESTINATION_NAME).get();
        destinationSpec.subscribe(
                TranslationKeys.AQ_SUBSCRIBER,
                ServiceCallIssueService.COMPONENT_NAME,
                Layer.DOMAIN);
    }


    private IssueType setSupportedIssueType() {
        return issueService.createIssueType(ServiceCallIssueService.ISSUE_TYPE_NAME, TranslationKeys.SERVICE_CALL_ISSUE_TYPE, ServiceCallIssueService.SERVICE_CALL_ISSUE_PREFIX);
    }

    private void createIssueTypeAndReasons(IssueType issueType) {
        IssueReason serviceCallFailed = issueService.createReason(ModuleConstants.REASON_FAILED, issueType,
                TranslationKeys.SERVICE_CALL_ISSUE_FAILED_REASON, TranslationKeys.SERVICE_CALL_ISSUE_FAILED_REASON_DESCRIPTION);
        IssueReason serviceCallPartialSucceed = issueService.createReason(ModuleConstants.REASON_PARTIAL_SUCCEED, issueType,
                TranslationKeys.SERVICE_CALL_ISSUE_PARTIAL_SUCCEED_REASON, TranslationKeys.SERVICE_CALL_ISSUE_PARTIAL_SUCCEED_REASON_DESCRIPTION);

        issueActionService.createActionType(ServiceCallIssueActionsFactory.ID, FailedAction.class.getName(), serviceCallFailed, CreationRuleActionPhase.NOT_APPLICABLE);
        issueActionService.createActionType(ServiceCallIssueActionsFactory.ID, PartialSucceedAction.class.getName(), serviceCallPartialSucceed, CreationRuleActionPhase.NOT_APPLICABLE);
        issueActionService.createActionType(ServiceCallIssueActionsFactory.ID, StartProcessAction.class.getName(), issueType, CreationRuleActionPhase.CREATE);
        issueActionService.createActionType(ServiceCallIssueActionsFactory.ID, RetryServiceCallAction.class.getName(), issueType, CreationRuleActionPhase.NOT_APPLICABLE);
    }

    private void run(Runnable runnable, String explanation, Logger logger) {
        doTry(
                explanation,
                runnable,
                logger
        );
    }
}
