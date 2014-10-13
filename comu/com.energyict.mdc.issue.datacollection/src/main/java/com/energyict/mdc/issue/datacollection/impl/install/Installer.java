package com.energyict.mdc.issue.datacollection.impl.install;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.DuplicateSubscriberNameException;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.impl.DataCollectionActionsFactory;
import com.energyict.mdc.issue.datacollection.impl.ModuleConstants;
import com.energyict.mdc.issue.datacollection.impl.actions.RetryCommunicationTaskAction;
import com.energyict.mdc.issue.datacollection.impl.actions.RetryCommunicationTaskNowAction;
import com.energyict.mdc.issue.datacollection.impl.actions.RetryConnectionTaskAction;
import com.energyict.mdc.issue.datacollection.impl.database.CreateIssueViewOperation;
import com.energyict.mdc.issue.datacollection.impl.i18n.MessageSeeds;

public class Installer {

    private final MessageService messageService;
    private final IssueService issueService;
    private final IssueActionService issueActionService;
    private final DataModel dataModel;

    public Installer(DataModel dataModel, IssueService issueService, IssueActionService issueActionService, MessageService messageService) {
        this.issueService = issueService;
        this.issueActionService = issueActionService;
        this.messageService = messageService;
        this.dataModel = dataModel;
    }

    public void install() {
        try {
            dataModel.install(true, false);
            new CreateIssueViewOperation(dataModel).execute();
        } catch (Exception ex){}
        setAQSubscriber();
        IssueType issueType = setSupportedIssueType();
        setDataCollectionReasons(issueType);
        createActionTypes();
    }

    private IssueType setSupportedIssueType() {
        return issueService.createIssueType(IssueDataCollectionService.ISSUE_TYPE_UUID, MessageSeeds.ISSUE_TYPE_DATA_COLELCTION);
    }

    private void setAQSubscriber() {
        DestinationSpec destinationSpec = messageService.getDestinationSpec(EventService.JUPITER_EVENTS).get();
        try {
            destinationSpec.subscribe(ModuleConstants.AQ_DATA_COLLECTION_EVENT_SUBSC);
        } catch (DuplicateSubscriberNameException e) {
            // subscriber already exists, ignoring
        }
        try {
            destinationSpec.subscribe(ModuleConstants.AQ_METER_READING_EVENT_SUBSC);
        } catch (DuplicateSubscriberNameException e) {
            // subscriber already exists, ignoring
        }
    }

    private void setDataCollectionReasons(IssueType issueType) {
        issueService.createReason(ModuleConstants.REASON_UNKNOWN_INBOUND_DEVICE, issueType, MessageSeeds.ISSUE_REASON_UNKNOWN_INBOUND_DEVICE);
        issueService.createReason(ModuleConstants.REASON_UNKNOWN_OUTBOUND_DEVICE, issueType, MessageSeeds.ISSUE_REASON_UNKNOWN_OUTBOUND_DEVICE);
        issueService.createReason(ModuleConstants.REASON_FAILED_TO_COMMUNICATE, issueType, MessageSeeds.ISSUE_REASON_FAILED_TO_COMMUNICATE);
        issueService.createReason(ModuleConstants.REASON_CONNECTION_SETUP_FAILED, issueType, MessageSeeds.ISSUE_REASON_CONNECTION_SETUP_FAILED);
        issueService.createReason(ModuleConstants.REASON_CONNECTION_FAILED, issueType, MessageSeeds.ISSUE_REASON_CONNECTION_FAILED);
        issueService.createReason(ModuleConstants.REASON_POWER_OUTAGE, issueType, MessageSeeds.ISSUE_REASON_POWER_OUTAGE);
        issueService.createReason(ModuleConstants.REASON_TYME_SYNC_FAILED, issueType, MessageSeeds.ISSUE_REASON_TIME_SYNC_FAILED);
        issueService.createReason(ModuleConstants.REASON_SLOPE_DETECTION, issueType, MessageSeeds.ISSUE_REASON_SLOPE_DETECTION);
    }

    private void createActionTypes(){
        IssueType type = null;
        issueActionService.createActionType(DataCollectionActionsFactory.ID, RetryCommunicationTaskAction.class.getName(), type);
        issueActionService.createActionType(DataCollectionActionsFactory.ID, RetryCommunicationTaskNowAction.class.getName(), type);
        issueActionService.createActionType(DataCollectionActionsFactory.ID, RetryConnectionTaskAction.class.getName(), type);
    }

}

