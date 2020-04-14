/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.events.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import com.google.common.collect.Lists;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.events.EventService.JUPITER_EVENTS;
import static com.elster.jupiter.messaging.MessageService.QueueTable.JUPITEREVENTS_RAW_QUEUE_TABLE;
import static com.elster.jupiter.orm.Version.version;

public class UpgraderV10_8 implements Upgrader {
    private static final int RETRY_DELAY = 60;
    private static final Version VERSION = version(10, 8);

    private final DataModel dataModel;
    private final MessageService messageService;

    @Inject
    public UpgraderV10_8(DataModel dataModel, MessageService messageService) {
        this.dataModel = dataModel;
        this.messageService = messageService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, VERSION);
        createAndActivateJupiterEventsTopic(dataModelUpgrader);
    }

    private void createAndActivateJupiterEventsTopic(DataModelUpgrader dataModelUpgrader) {
        Optional<DestinationSpec> oldDestinationSpec = messageService.getDestinationSpec(JUPITER_EVENTS);
        List<SubscriberSpec> oldsubscribers = null;
        if (oldDestinationSpec.isPresent()) {
            DestinationSpec destinationSpec = oldDestinationSpec.get();
            oldsubscribers = Lists.newArrayList(destinationSpec.getSubscribers());
            destinationSpec.unSubscribe(JUPITER_EVENTS);
            destinationSpec.delete();
        }

        DestinationSpec destinationSpec = getRawTopicTableSpec().createDestinationSpec(JUPITER_EVENTS, RETRY_DELAY);
        destinationSpec.activate();
        subscribeToNewDestinationSpec(destinationSpec, oldsubscribers);
    }

    private QueueTableSpec getRawTopicTableSpec() {
        return messageService.getQueueTableSpec(JUPITEREVENTS_RAW_QUEUE_TABLE.getQueueTableName()).get();
    }

    private void subscribeToNewDestinationSpec(DestinationSpec destinationSpec, List<SubscriberSpec> subscribers) {
        if (subscribers != null) {
            subscribers.forEach(subscriberSpec -> destinationSpec.subscribe(subscriberSpec));
        }
    }
}
