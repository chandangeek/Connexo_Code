/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.impl.install;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.issue.share.entity.CreationRuleActionPhase;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.DuplicateSubscriberNameException;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.impl.DataCollectionActionsFactory;
import com.energyict.mdc.issue.datacollection.impl.ModuleConstants;
import com.energyict.mdc.issue.datacollection.impl.actions.CloseIssueAction;
import com.energyict.mdc.issue.datacollection.impl.actions.RetryCommunicationTaskAction;
import com.energyict.mdc.issue.datacollection.impl.actions.RetryCommunicationTaskNowAction;
import com.energyict.mdc.issue.datacollection.impl.actions.RetryConnectionTaskAction;
import com.energyict.mdc.issue.datacollection.impl.database.CreateIssueViewOperation;
import com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription;
import com.energyict.mdc.issue.datacollection.impl.event.DataCollectionResolveEventDescription;
import com.energyict.mdc.issue.datacollection.impl.i18n.TranslationKeys;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import static com.elster.jupiter.messaging.DestinationSpec.whereCorrelationId;

public class Installer implements FullInstaller {
    private static final Logger LOG = Logger.getLogger("DataCollectionIssueInstaller");

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
        run(() -> new CreateIssueViewOperation(dataModel).execute(), "database schema. Execute command 'ddl " + IssueDataCollectionService.COMPONENT_NAME + "' and apply the sql script manually", logger);
        run(this::setAQSubscriber, "aq subscribers", logger);
        run(() -> {
            IssueType issueType = setSupportedIssueType();
            setDataCollectionReasons(issueType);
        }, "issue reasons and action types", logger);
        run(this::publishEvents, "event publishing", logger);

    }

    private void publishEvents() {
        Set<EventType> eventTypesToPublish = new HashSet<>();
        eventService.getEventType("com/elster/jupiter/metering/meterreading/CREATED")
                .ifPresent(eventTypesToPublish::add);
        for (DataCollectionEventDescription dataCollectionEventDescription : DataCollectionEventDescription.values()) {
            eventService.getEventType(dataCollectionEventDescription.getTopic()).ifPresent(eventTypesToPublish::add);
        }
        for (DataCollectionResolveEventDescription dataCollectionEventDescription : DataCollectionResolveEventDescription
                .values()) {
            eventService.getEventType(dataCollectionEventDescription.getTopic()).ifPresent(eventTypesToPublish::add);
        }
        for (EventType eventType : eventTypesToPublish) {
            eventType.setPublish(true);
            eventType.update();
        }
    }

    private IssueType setSupportedIssueType() {
        return issueService.createIssueType(IssueDataCollectionService.DATA_COLLECTION_ISSUE, TranslationKeys.ISSUE_TYPE_DATA_COLLECTION, IssueDataCollectionService.DATA_COLLECTION_ISSUE_PREFIX);
    }

    private void setAQSubscriber() {
        DestinationSpec destinationSpec = messageService.getDestinationSpec(EventService.JUPITER_EVENTS).get();
        try {
            destinationSpec.subscribe(
                    TranslationKeys.AQ_DATA_COLLECTION_EVENT_SUBSC,
                    IssueDataCollectionService.COMPONENT_NAME, Layer.DOMAIN,
                    whereCorrelationId()
                            .like("com/energyict/mdc/connectiontask/%")
                            .or(whereCorrelationId().isEqualTo("com/energyict/mdc/device/data/device/CREATED")
                            .or(whereCorrelationId().isEqualTo("com/energyict/mdc/inboundcommunication/UNKNOWNDEVICE"))
                            .or(whereCorrelationId().isEqualTo("com/energyict/mdc/outboundcommunication/UNKNOWNSLAVEDEVICE"))));
        } catch (DuplicateSubscriberNameException e) {
            // subscriber already exists, ignoring
        }
    }

    private void setDataCollectionReasons(IssueType issueType) {
        issueService.createReason(ModuleConstants.REASON_UNKNOWN_INBOUND_DEVICE, issueType,
                TranslationKeys.ISSUE_REASON_UNKNOWN_INBOUND_DEVICE, TranslationKeys.ISSUE_REASON_DESCRIPTION_UNKNOWN_INBOUND_DEVICE);
        issueService.createReason(ModuleConstants.REASON_UNKNOWN_OUTBOUND_DEVICE, issueType,
                TranslationKeys.ISSUE_REASON_UNKNOWN_OUTBOUND_DEVICE, TranslationKeys.ISSUE_REASON_DESCRIPTION_UNKNOWN_OUTBOUND_DEVICE);

        IssueReason failedToCommunicateReason = issueService.createReason(ModuleConstants.REASON_FAILED_TO_COMMUNICATE, issueType,
                TranslationKeys.ISSUE_REASON_FAILED_TO_COMMUNICATE, TranslationKeys.ISSUE_REASON_DESCRIPTION_FAILED_TO_COMMUNICATE);
        issueActionService.createActionType(DataCollectionActionsFactory.ID, RetryCommunicationTaskAction.class.getName(), failedToCommunicateReason);

        IssueReason connectionSetupFailedReason = issueService.createReason(ModuleConstants.REASON_CONNECTION_SETUP_FAILED, issueType,
                TranslationKeys.ISSUE_REASON_CONNECTION_SETUP_FAILED, TranslationKeys.ISSUE_REASON_DESCRIPTION_CONNECTION_SETUP_FAILED);
        issueActionService.createActionType(DataCollectionActionsFactory.ID, RetryCommunicationTaskNowAction.class.getName(), failedToCommunicateReason);

        IssueReason connectionFailedReason = issueService.createReason(ModuleConstants.REASON_CONNECTION_FAILED, issueType,
                TranslationKeys.ISSUE_REASON_CONNECTION_FAILED, TranslationKeys.ISSUE_REASON_DESCRIPTION_CONNECTION_FAILED);
        issueActionService.createActionType(DataCollectionActionsFactory.ID, RetryConnectionTaskAction.class.getName(), connectionSetupFailedReason);
        issueActionService.createActionType(DataCollectionActionsFactory.ID, RetryConnectionTaskAction.class.getName(), connectionFailedReason);

        issueService.createReason(ModuleConstants.REASON_POWER_OUTAGE, issueType,
                TranslationKeys.ISSUE_REASON_POWER_OUTAGE, TranslationKeys.ISSUE_REASON_DESCRIPTION_POWER_OUTAGE);
        issueService.createReason(ModuleConstants.REASON_TYME_SYNC_FAILED, issueType,
                TranslationKeys.ISSUE_REASON_TIME_SYNC_FAILED, TranslationKeys.ISSUE_REASON_DESCRIPTION_TIME_SYNC_FAILED);
        issueActionService.createActionType(DataCollectionActionsFactory.ID, CloseIssueAction.class.getName(), issueType, CreationRuleActionPhase.OVERDUE);
    }

    private void run(Runnable runnable, String explanation, Logger logger) {
        doTry(
                explanation,
                runnable,
                logger
        );
    }

}