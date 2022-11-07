/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.issue.impl.database;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.impl.event.EventType;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.util.EnumSet;

import static com.elster.jupiter.issue.impl.module.TranslationKeys.AQ_BULK_ISSUE_CLOSE_EVENT_SUBSC;

public class UpgraderV10_9_21 implements Upgrader {
    private final MessageService messageService;
    private final DataModel dataModel;
    private final EventService eventService;

    @Inject
    UpgraderV10_9_21(MessageService messageService, DataModel dataModel, EventService eventService) {
        this.messageService = messageService;
        this.dataModel = dataModel;
        this.eventService = eventService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 9, 21));
        installNewEventTypes();
        addJupiterEventSubscribers();
    }

    private void addJupiterEventSubscribers() {
        this.messageService.getDestinationSpec(EventService.JUPITER_EVENTS)
                .ifPresent(jupiterEvents -> {
                    boolean subscriberExists = jupiterEvents.getSubscribers()
                            .stream()
                            .anyMatch(s -> s.getName().equals(AQ_BULK_ISSUE_CLOSE_EVENT_SUBSC.getKey()));

                    if (!subscriberExists) {
                        jupiterEvents.subscribe(AQ_BULK_ISSUE_CLOSE_EVENT_SUBSC,
                                IssueService.COMPONENT_NAME,
                                Layer.DOMAIN,
                                DestinationSpec.whereCorrelationId().isEqualTo("com/elster/jupiter/issues/BULK_CLOSE")
                        );
                    }
                });
    }

    private void installNewEventTypes() {
        EnumSet.of(EventType.BULK_CLOSE_ISSUE).forEach(eventType -> eventType.install(eventService));
    }
}

