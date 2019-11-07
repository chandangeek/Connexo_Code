package com.energyict.mdc.issue.datavalidation.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.DuplicateSubscriberNameException;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.issue.datavalidation.impl.event.DataValidationEventDescription;
import com.energyict.mdc.issue.datavalidation.impl.event.DataValidationEventHandlerFactory;

import javax.inject.Inject;

import static com.elster.jupiter.messaging.DestinationSpec.whereCorrelationId;

public class UpgraderV10_7 implements Upgrader {

    private final DataModel dataModel;
    private final MessageService messageService;

    @Inject
    UpgraderV10_7(final DataModel dataModel, final MessageService messageService) {
        this.dataModel = dataModel;
        this.messageService = messageService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 7));
        updateAQSubscriber();
    }

    private void updateAQSubscriber() {
        DestinationSpec destinationSpec = messageService.getDestinationSpec(EventService.JUPITER_EVENTS).get();
        try {
            if (destinationSpec.isActive()) {
                destinationSpec.unSubscribe(DataValidationEventHandlerFactory.AQ_DATA_VALIDATION_EVENT_SUBSCRIBER);
            }
            destinationSpec.subscribe(
                    TranslationKeys.AQ_SUBSCRIBER,
                    IssueDataValidationService.COMPONENT_NAME,
                    Layer.DOMAIN,
                    whereCorrelationId()
                            .like("com/elster/jupiter/validation/suspect/%")
                            .or(whereCorrelationId().isEqualTo(DataValidationEventDescription.READINGQUALITY_DELETED.getTopic())
                                    .or(whereCorrelationId().isEqualTo(DataValidationEventDescription.CANNOT_ESTIMATE_DATA.getTopic()))
                                    .or(whereCorrelationId().isEqualTo(DataValidationEventDescription.SUSPECT_VALUE_CREATED.getTopic()))));
        } catch (DuplicateSubscriberNameException e) {
            // subsriber already exists, ignoring
        }
    }
}
