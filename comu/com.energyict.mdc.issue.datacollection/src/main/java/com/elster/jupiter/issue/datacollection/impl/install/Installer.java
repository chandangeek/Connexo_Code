package com.elster.jupiter.issue.datacollection.impl.install;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.datacollection.impl.ModuleConstants;
import com.elster.jupiter.issue.share.entity.IssueEventType;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Installer {

    private final MessageService messageService;
    private final IssueService issueService;
    private final EventService eventService;
    private final Thesaurus thesaurus;

    public Installer(IssueService issueService, MessageService messageService, EventService eventService, Thesaurus thesaurus) {
        this.issueService = issueService;
        this.messageService = messageService;
        this.eventService = eventService;
        this.thesaurus = thesaurus;
    }

    public void install() {
        IssueType issueType = setSupportedIssueType();
        setDataCollectionReasons(issueType);
        setAQSubscriber();
        setEventTopics();
        setTranslations();
    }

    private IssueType setSupportedIssueType(){
        return issueService.createIssueType(ModuleConstants.ISSUE_TYPE_UUID, ModuleConstants.ISSUE_TYPE);
    }

    private void setAQSubscriber() {
        messageService.getDestinationSpec(EventService.JUPITER_EVENTS).get().subscribe(ModuleConstants.AQ_SUBSCRIBER_NAME);
    }

    private void setDataCollectionReasons(IssueType issueType) {
        issueService.createReason("Unknown inbound device", issueType);
        issueService.createReason("Unknown outbound device", issueType);
        issueService.createReason("Failed to communicate", issueType);
        issueService.createReason("Connection setup failed", issueType);
        issueService.createReason("Connection failed", issueType);
        issueService.createReason("Power outage", issueType);
        issueService.createReason("Time sync failed", issueType);
    }

    // TODO remove it when MDC will register topics
    @Deprecated
    private void setEventTopics() {
        for (IssueEventType eventType : IssueEventType.values()) {
            try {
                eventType.install(eventService);
            } catch (Exception e) {
                System.out.println("Could not create event type : " + eventType.name());
            }
        }
    }

    private void setTranslations(){
        List<Translation> translations = new ArrayList<>(com.elster.jupiter.issue.datacollection.impl.install.MessageSeeds.values().length);
        for (com.elster.jupiter.issue.datacollection.impl.install.MessageSeeds messageSeed : com.elster.jupiter.issue.datacollection.impl.install.MessageSeeds.values()) {
            SimpleNlsKey nlsKey = SimpleNlsKey.key(ModuleConstants.COMPONENT_NAME, Layer.DOMAIN, messageSeed.getKey()).defaultMessage(messageSeed.getDefaultFormat());
            translations.add(toTranslation(nlsKey, Locale.ENGLISH, messageSeed.getDefaultFormat()));
        }
        thesaurus.addTranslations(translations);
    }

    private Translation toTranslation(final SimpleNlsKey nlsKey, final Locale locale, final String translation) {
        return new Translation() {
            @Override
            public NlsKey getNlsKey() {
                return nlsKey;
            }

            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String getTranslation() {
                return translation;
            }
        };
    }
}
