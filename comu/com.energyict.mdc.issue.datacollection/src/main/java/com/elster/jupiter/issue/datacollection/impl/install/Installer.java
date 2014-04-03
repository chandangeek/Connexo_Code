package com.elster.jupiter.issue.datacollection.impl.install;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.datacollection.impl.ModuleConstants;
import com.elster.jupiter.issue.share.entity.IssueEventType;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.MessageService;

public class Installer {

    private final MessageService messageService;
    private final IssueService issueService;
    private final EventService eventService;

    public Installer(IssueService issueService, MessageService messageService, EventService eventService) {
        this.issueService = issueService;
        this.messageService = messageService;
        this.eventService = eventService;
    }

    public void install() {
        setSupportedIssueType();
        setDataCollectionReasons();
        setAQSubscriber();
        setEventTopics();
    }

    private void setSupportedIssueType(){
        issueService.createIssueType(ModuleConstants.ISSUE_TYPE);
    }

    private void setAQSubscriber() {
        messageService.getDestinationSpec(EventService.JUPITER_EVENTS).get().subscribe(ModuleConstants.AQ_SUBSCRIBER_NAME);
    }

    private void setDataCollectionReasons() {
        // TODO set correct reasons
    }

    private void setEventTopics() {
        for (IssueEventType eventType : IssueEventType.values()) {
            try {
                eventType.install(eventService);
            } catch (Exception e) {
                System.out.println("Could not create event type : " + eventType.name());
            }
        }
    }
}
