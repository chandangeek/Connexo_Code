package com.elster.jupiter.issue.module;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.IssueEventType;
import com.elster.jupiter.issue.IssueService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.orm.DataModel;

public class Installer {
    /*
    If you want to add EventService see @com.elster.jupiter.parties.impl.Installer class file
     */
    private final DataModel dataModel;
    private final IssueService issueService;
    private MessageService messageService;
    private AppService appService;
    private CronExpressionParser cronExpressionParser;
    private AppServer appServer;

    public Installer (IssueService issueService, DataModel dataModel, MessageService messageService,
                      AppService appService, CronExpressionParser cronExpressionParser) {
        this.issueService = issueService;
        this.messageService = messageService;
        this.appService = appService;
        this.cronExpressionParser = cronExpressionParser;
        this.dataModel = dataModel;
    }

    public void install(boolean executeDDL, boolean store) {
        dataModel.install(executeDDL, store);
        setDefaultReasons();
        setDefaultStatuses();
        setAQSabscriber();
        setAppServer();
    }

    private void setDefaultReasons(){
        this.issueService.createIssueReason("Unknown inbound device", IssueEventType.UNKNOWN_INBOUND_DEVICE.topic());
        this.issueService.createIssueReason("Unknown outbound device", IssueEventType.UNKNOWN_OUTBOUND_DEVICE.topic());
        this.issueService.createIssueReason("Failed to communicate", IssueEventType.DEVICE_COMMUNICATION_FAILURE.topic());
        this.issueService.createIssueReason("Connection lost", IssueEventType.DEVICE_COMMUNICATION_FAILURE.topic());
    }

    private void setDefaultStatuses(){
        this.issueService.createIssueStatus("Open");
        this.issueService.createIssueStatus("Closed");
        this.issueService.createIssueStatus("Rejected");
        this.issueService.createIssueStatus("In progress");
    }

    private void setAQSabscriber() {
        DestinationSpec destinationSpec = messageService.getDestinationSpec(EventService.JUPITER_EVENTS).get();
        SubscriberSpec subscriberSpec = destinationSpec.subscribe("ISSUECREATOR");
    }

    private void setAppServer() {
        this.appServer = appService.createAppServer("issueAppServer", cronExpressionParser.parse("0 0 * * * ? *"));
        int numberOfThreads = 1;
        SubscriberSpec subscriberSpec = messageService.getSubscriberSpec(EventService.JUPITER_EVENTS, "ISSUECREATOR").get();
        SubscriberExecutionSpec subscriberExecutionSpec = appServer.createSubscriberExecutionSpec(subscriberSpec, numberOfThreads);

    }
}
