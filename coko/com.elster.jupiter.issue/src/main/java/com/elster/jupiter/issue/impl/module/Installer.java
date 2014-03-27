package com.elster.jupiter.issue.impl.module;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.impl.database.CreateIssueViewOperation;
import com.elster.jupiter.issue.impl.event.EventConst;
import com.elster.jupiter.issue.share.entity.IssueEventType;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModel;

public class Installer {

    private final DataModel dataModel;
    private final MessageService messageService;
    private final IssueService issueService;

    public Installer(DataModel dataModel, IssueService issueService, MessageService messageService) {
        this.dataModel = dataModel;
        this.issueService = issueService;
        this.messageService = messageService;
    }

    public void install(boolean executeDDL) {
        dataModel.install(executeDDL, false);
        createCommonIssueView();
        setDefaultReasons();
        setDefaultStatuses();
        setAQSubscriber();
    }

    private void createCommonIssueView(){
        CreateIssueViewOperation.init(dataModel).execute();
    }

    private void setDefaultReasons(){
        issueService.createReason("Unknown inbound device", IssueEventType.UNKNOWN_INBOUND_DEVICE.topic());
        issueService.createReason("Unknown outbound device", IssueEventType.UNKNOWN_OUTBOUND_DEVICE.topic());
        issueService.createReason("Failed to communicate", IssueEventType.DEVICE_COMMUNICATION_FAILURE.topic());
        issueService.createReason("Connection setup failed", IssueEventType.DEVICE_CONNECTION_SETUP_FAILURE.topic());
        issueService.createReason("Connection failed", IssueEventType.DEVICE_CONNECTION_FAILURE.topic());
    }

    private void setDefaultStatuses(){
        issueService.createStatus("Open", false);
        issueService.createStatus("Resolved", true);
        issueService.createStatus("Won't fix", true);
    }

    private void setAQSubscriber() {
        messageService.getDestinationSpec(EventService.JUPITER_EVENTS).get().subscribe(EventConst.AQ_SUBSCRIBER_NAME);
    }
}
