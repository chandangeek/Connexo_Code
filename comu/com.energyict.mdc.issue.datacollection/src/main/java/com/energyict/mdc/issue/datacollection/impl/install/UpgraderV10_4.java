/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.impl.install;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.DuplicateSubscriberNameException;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.impl.ModuleConstants;
import com.energyict.mdc.issue.datacollection.impl.i18n.TranslationKeys;

import javax.inject.Inject;

import static com.elster.jupiter.messaging.DestinationSpec.whereCorrelationId;

public class UpgraderV10_4 implements Upgrader {
    private final EventService eventService;
    private final MessageService messageService;
    private final DataModel dataModel;
    private final IssueService issueService;

    @Inject
    public UpgraderV10_4(EventService eventService, MessageService messageService, DataModel dataModel, IssueService issueService) {
        this.eventService = eventService;
        this.messageService = messageService;
        this.dataModel = dataModel;
        this.issueService = issueService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 4));
        this.setAQSubscriber();
        this.createEventTypes();
        this.createNewReason();
    }

    private void createNewReason() {
        IssueType issueType = issueService.findIssueType(IssueDataCollectionService.DATA_COLLECTION_ISSUE)
                .orElseGet(() -> issueService.createIssueType(IssueDataCollectionService.DATA_COLLECTION_ISSUE, TranslationKeys.ISSUE_TYPE_DATA_COLLECTION, IssueDataCollectionService.DATA_COLLECTION_ISSUE_PREFIX));
        issueService.createReason(ModuleConstants.REASON_UNREGISTERED_DEVICE, issueType, TranslationKeys.ISSUE_REASON_UNREGISTERED_DEVICE, TranslationKeys.ISSUE_REASON_DESCRIPTION_UNREGISTERED_DEVICE);
    }

    private void setAQSubscriber() {
        DestinationSpec destinationSpec = messageService.getDestinationSpec(EventService.JUPITER_EVENTS).get();
        try {
            if (destinationSpec.isActive()) {
                destinationSpec.unSubscribe(ModuleConstants.AQ_DATA_COLLECTION_EVENT_SUBSC);
                destinationSpec.unSubscribe(ModuleConstants.AQ_DELAYED_ISSUE_SUBSC);
            }
            destinationSpec.subscribe(
                    TranslationKeys.AQ_DATA_COLLECTION_EVENT_SUBSC,
                    IssueDataCollectionService.COMPONENT_NAME, Layer.DOMAIN,
                    whereCorrelationId()
                            .like("com/energyict/mdc/connectiontask/%")
                            .or(whereCorrelationId().isEqualTo("com/energyict/mdc/device/data/device/CREATED")
                                    .or(whereCorrelationId().isEqualTo("com/energyict/mdc/inboundcommunication/UNKNOWNDEVICE"))
                                    .or(whereCorrelationId().isEqualTo("com/energyict/mdc/topology/UNREGISTEREDFROMGATEWAY"))
                                    .or(whereCorrelationId().isEqualTo("com/energyict/mdc/topology/REGISTEREDTOGATEWAY"))
                                    .or(whereCorrelationId().isEqualTo("com/energyict/mdc/outboundcommunication/UNKNOWNSLAVEDEVICE"))));
            destinationSpec.subscribe(
                    TranslationKeys.AQ_DELAYED_ISSUE_SUBSC,
                    IssueDataCollectionService.COMPONENT_NAME, Layer.DOMAIN,
                    whereCorrelationId().isEqualTo("com/energyict/mdc/issue/datacollection/UNREGISTEREDFROMGATEWAYDELAYED"));
        } catch (DuplicateSubscriberNameException e) {
            // subscriber already exists, ignoring
        }
    }

    private void createEventTypes() {
        for (com.energyict.mdc.issue.datacollection.impl.event.EventType eventType : com.energyict.mdc.issue.datacollection.impl.event.EventType.values()) {
            eventType.createIfNotExists(eventService);
        }
    }
}
