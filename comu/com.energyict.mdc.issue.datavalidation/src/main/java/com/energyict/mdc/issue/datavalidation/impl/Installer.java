package com.energyict.mdc.issue.datavalidation.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.util.exception.ExceptionCatcher;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.issue.datavalidation.impl.event.DataValidationEventDescription;
import com.energyict.mdc.issue.datavalidation.impl.event.DataValidationEventHandlerFactory;
import com.google.inject.Inject;

import static com.elster.jupiter.messaging.DestinationSpec.whereCorrelationId;

class Installer implements FullInstaller {

    private final IssueService issueService;
    private final DataModel dataModel;
    private final EventService eventService;
    private final MessageService messageService;

    @Inject
    Installer(DataModel dataModel, IssueService issueService, EventService eventService, MessageService messageService) {
        this.dataModel = dataModel;
        this.issueService = issueService;
        this.eventService = eventService;
        this.messageService = messageService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader) {
        ExceptionCatcher.executing(
                () -> this.installDataModel(dataModelUpgrader),
                this::createIssueTypeAndReasons,
                this::setAQSubscriber,
                this::publishEvents)
                .andHandleExceptionsWith(Throwable::printStackTrace)
                .execute();
    }

    private void installDataModel(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        new CreateIssueViewOperation(dataModel).execute();
    }

    private void createIssueTypeAndReasons() {
        IssueType type = issueService.createIssueType(IssueDataValidationService.ISSUE_TYPE_NAME, TranslationKeys.DATA_VALIDATION_ISSUE_TYPE);
        issueService.createReason(IssueDataValidationService.DATA_VALIDATION_ISSUE_REASON, type,
                TranslationKeys.DATA_VALIDATION_ISSUE_REASON, TranslationKeys.DATA_VALIDATION_ISSUE_REASON_DESCRIPTION);
    }

    private void publishEvents() {
        for (DataValidationEventDescription eventDescription : DataValidationEventDescription.values()) {
            eventService.getEventType(eventDescription.getTopic()).ifPresent(eventType -> {
                eventType.setPublish(true);
                eventType.update();
            });
        }
    }

    private void setAQSubscriber() {
        DestinationSpec destinationSpec = messageService.getDestinationSpec(EventService.JUPITER_EVENTS).get();
        destinationSpec.subscribe(DataValidationEventHandlerFactory.AQ_DATA_VALIDATION_EVENT_SUBSCRIBER,
                whereCorrelationId().isEqualTo(DataValidationEventDescription.CANNOT_ESTIMATE_DATA.getTopic())
                        .or(whereCorrelationId().isEqualTo(DataValidationEventDescription.READINGQUALITY_DELETED.getTopic())));
    }
}
