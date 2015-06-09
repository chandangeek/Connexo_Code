package com.energyict.mdc.issue.datavalidation.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.exception.ExceptionCatcher;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.issue.datavalidation.MessageSeeds;

public class Installer {

    private final MessageService messageService;
    private final IssueService issueService;
    private final DataModel dataModel;
    private final EventService eventService;
    private final Thesaurus thesaurus;

    public Installer(DataModel dataModel, IssueService issueService, MessageService messageService, EventService eventService, Thesaurus thesaurus) {
        this.issueService = issueService;
        this.messageService = messageService;
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.thesaurus = thesaurus;
    }

    public void install() {
        ExceptionCatcher.executing(
//                this::installDataModel,
                this::createIssueTypeAndReasons
        ).andHandleExceptionsWith(Throwable::printStackTrace)
                .execute();

//        run(this::setAQSubscriber, "aq subscribers");
//        run(this::publishEvents, "event publishing");
    }
    
    private void installDataModel() {
        dataModel.install(true, true);
        new CreateIssueViewOperation(dataModel).execute();
    }
    
    private void createIssueTypeAndReasons() {
        IssueType type = issueService.createIssueType(IssueDataValidationService.ISSUE_TYPE_NAME, MessageSeeds.DATA_VALIDATION_ISSUE_TYPE);
        issueService.createReason(IssueDataValidationService.DATA_VALIDATION_ISSUE_REASON, type, MessageSeeds.DATA_VALIDATION_ISSUE_REASON);
    }

//    private void publishEvents() {
//        Set<EventType> eventTypesToPublish = new HashSet<>();
//        eventService.getEventType("com/elster/jupiter/metering/meterreading/CREATED").ifPresent(eventTypesToPublish::add);
//        for (DataCollectionEventDescription dataCollectionEventDescription : DataCollectionEventDescription.values()) {
//            eventService.getEventType(dataCollectionEventDescription.getTopic()).ifPresent(eventTypesToPublish::add);
//        }
//        for (DataCollectionResolveEventDescription dataCollectionEventDescription : DataCollectionResolveEventDescription.values()) {
//            eventService.getEventType(dataCollectionEventDescription.getTopic()).ifPresent(eventTypesToPublish::add);
//        }
//        for (EventType eventType : eventTypesToPublish) {
//            eventType.setPublish(true);
//            eventType.save();
//        }
//    }



//    private void setAQSubscriber() {
//        addTranslation(IssueDataCollectionService.COMPONENT_NAME, ModuleConstants.AQ_DATA_COLLECTION_EVENT_SUBSC, ModuleConstants.AQ_DATA_COLLECTION_EVENT_DISPLAYNAME);
//        addTranslation(IssueDataCollectionService.COMPONENT_NAME, ModuleConstants.AQ_METER_READING_EVENT_SUBSC, ModuleConstants.AQ_METER_READING_EVENT_DISPLAYNAME);
//
//        DestinationSpec destinationSpec = messageService.getDestinationSpec(EventService.JUPITER_EVENTS).get();
//        try {
//            destinationSpec.subscribe(ModuleConstants.AQ_DATA_COLLECTION_EVENT_SUBSC);
//        } catch (DuplicateSubscriberNameException e) {
//            // subscriber already exists, ignoring
//        }
//        try {
//            destinationSpec.subscribe(ModuleConstants.AQ_METER_READING_EVENT_SUBSC);
//        } catch (DuplicateSubscriberNameException e) {
//            // subscriber already exists, ignoring
//        }
//    }
}

