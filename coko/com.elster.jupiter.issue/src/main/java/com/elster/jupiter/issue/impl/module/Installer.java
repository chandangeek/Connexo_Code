package com.elster.jupiter.issue.impl.module;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.impl.database.CreateIssueViewOperation;
import com.elster.jupiter.issue.impl.event.EventConst;
import com.elster.jupiter.issue.share.entity.IssueEventType;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.service.IssueMainService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModel;

public class Installer {

    private final DataModel dataModel;
    private final MessageService messageService;
    private final IssueMainService issueMainService;

    public Installer(DataModel dataModel, IssueMainService issueMainService, MessageService messageService) {
        this.dataModel = dataModel;
        this.issueMainService = issueMainService;
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
        IssueReason reason = new IssueReason();
        reason.setName("Unknown inbound device");
        reason.setTopic(IssueEventType.UNKNOWN_INBOUND_DEVICE.topic());
        issueMainService.save(reason);

        reason.setName("Unknown outbound device");
        reason.setTopic(IssueEventType.UNKNOWN_OUTBOUND_DEVICE.topic());
        issueMainService.save(reason);

        reason.setName("Failed to communicate");
        reason.setTopic(IssueEventType.DEVICE_COMMUNICATION_FAILURE.topic());
        issueMainService.save(reason);

        reason.setName("Connection setup failed");
        reason.setTopic(IssueEventType.DEVICE_CONNECTION_SETUP_FAILURE.topic());
        issueMainService.save(reason);

        reason.setName("Connection failed");
        reason.setTopic(IssueEventType.DEVICE_CONNECTION_FAILURE.topic());
        issueMainService.save(reason);
    }

    private void setDefaultStatuses(){
        IssueStatus status = new IssueStatus();
        status.setFinal(false);
        status.setName("Open");
        issueMainService.save(status);

        status.setFinal(true);
        status.setName("Resolved");
        issueMainService.save(status);

        status.setFinal(true);
        status.setName("Won't fix");
        issueMainService.save(status);
    }

    private void setAQSubscriber() {
        messageService.getDestinationSpec(EventService.JUPITER_EVENTS).get().subscribe(EventConst.AQ_SUBSCRIBER_NAME);
    }
}
