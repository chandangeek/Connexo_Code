package com.elster.jupiter.issue.datacollection.impl.install;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.datacollection.impl.ModuleConstants;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Thesaurus;

public class Installer {

    private final MessageService messageService;
    private final IssueService issueService;
    private final EventService eventService;

    public Installer(IssueService issueService, MessageService messageService, EventService eventService, Thesaurus thesaurus) {
        this.issueService = issueService;
        this.messageService = messageService;
        this.eventService = eventService;
    }

    public void install() {
        IssueType issueType = setSupportedIssueType();
        setDataCollectionReasons(issueType);
        setAQSubscriber();
    }

    private IssueType setSupportedIssueType(){
        return issueService.createIssueType(ModuleConstants.ISSUE_TYPE_UUID, ModuleConstants.ISSUE_TYPE);
    }

    private void setAQSubscriber() {
        DestinationSpec destinationSpec = messageService.getDestinationSpec(EventService.JUPITER_EVENTS).get();
        destinationSpec.subscribe(ModuleConstants.AQ_DATA_COLLECTION_EVENT_SUBSC);
        destinationSpec.subscribe(ModuleConstants.AQ_METER_READING_EVENT_SUBSC);
    }

    private void setDataCollectionReasons(IssueType issueType) {
        issueService.createReason("Unknown inbound device", issueType);
        issueService.createReason("Unknown outbound device", issueType);
        issueService.createReason("Failed to communicate", issueType);
        issueService.createReason("Connection setup failed", issueType);
        issueService.createReason("Connection failed", issueType);
        issueService.createReason("Power outage", issueType);
        issueService.createReason("Time sync failed", issueType);
        issueService.createReason("Slope detection", issueType);
    }
}
