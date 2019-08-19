/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.entity.CreationRuleActionPhase;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.webservice.issue.WebServiceIssueService;
import com.elster.jupiter.webservice.issue.impl.actions.CloseIssueAction;
import com.elster.jupiter.webservice.issue.impl.actions.StartProcessWebServiceIssueAction;
import com.elster.jupiter.webservice.issue.impl.actions.WebServiceIssueActionsFactory;
import com.elster.jupiter.webservice.issue.impl.event.WebServiceEventDescription;

import com.google.inject.Inject;

import java.util.Arrays;
import java.util.Set;
import java.util.logging.Logger;

import static com.elster.jupiter.messaging.DestinationSpec.whereCorrelationId;

@LiteralSql
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
        doTry("Create all web service issues view", this::createView, logger);
        doTry("Create web service issue type, reasons and actions", this::createIssueTypeReasonsAndActions, logger);
        doTry("Create web service issue event subscriber", this::setAQSubscriber, logger);
    }

    private void createView() {
        execute(dataModel,
                "create or replace view " + TableSpecs.WSI_ISSUE_ALL.name() + " as " +
                "select * from " + TableSpecs.WSI_ISSUE_OPEN.name() + " union select * from " + TableSpecs.WSI_ISSUE_HISTORY.name());
    }

    private void createIssueTypeReasonsAndActions() {
        IssueType type = issueService.createIssueType(WebServiceIssueService.ISSUE_TYPE_NAME,
                TranslationKeys.WEB_SERVICE_ISSUE_TYPE, WebServiceIssueService.COMPONENT_NAME);
        issueService.createReason(WebServiceIssueService.WEB_SERVICE_ISSUE_REASON, type,
                TranslationKeys.WEB_SERVICE_ISSUE_REASON, TranslationKeys.WEB_SERVICE_ISSUE_REASON_DESCRIPTION);
        issueActionService.createActionType(WebServiceIssueActionsFactory.ID, CloseIssueAction.class.getName(), type, CreationRuleActionPhase.OVERDUE);
        issueActionService.createActionType(WebServiceIssueActionsFactory.ID, StartProcessWebServiceIssueAction.class.getName(), type, CreationRuleActionPhase.CREATE);
    }

    private void setAQSubscriber() {
        DestinationSpec destinationSpec = messageService.getDestinationSpec(EventService.JUPITER_EVENTS).get();
        Condition topicsCondition = Arrays.stream(WebServiceEventDescription.values())
                .map(WebServiceEventDescription::getTopics)
                .flatMap(Set::stream)
                .distinct()
                .map(whereCorrelationId()::isEqualTo)
                .reduce(Condition::or)
                .orElse(Condition.FALSE);
        destinationSpec.subscribe(
                TranslationKeys.SUBSCRIBER,
                WebServiceIssueService.COMPONENT_NAME,
                Layer.DOMAIN,
                topicsCondition);
    }
}
