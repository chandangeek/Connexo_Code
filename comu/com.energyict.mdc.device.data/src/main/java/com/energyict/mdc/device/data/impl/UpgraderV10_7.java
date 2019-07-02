/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.LoadProfileService;

import javax.inject.Inject;
import java.util.Optional;

public class UpgraderV10_7 implements Upgrader {

    private final DataModel dataModel;
    private final MessageService messageService;

    @Inject
    public UpgraderV10_7(DataModel dataModel, MessageService messageService) {
        this.dataModel = dataModel;
        this.messageService = messageService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 7));
        createMessageHandlerLP();
    }

    private void createMessageHandlerLP() {
        QueueTableSpec defaultQueueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        Optional<DestinationSpec> destinationSpecOptional = messageService.getDestinationSpec(LoadProfileService.BULK_LOADPROFILE_QUEUE_DESTINATION);
        if (!destinationSpecOptional.isPresent()) {
            DestinationSpec queue = defaultQueueTableSpec.createDestinationSpec(LoadProfileService.BULK_LOADPROFILE_QUEUE_DESTINATION, Installer.DEFAULT_RETRY_DELAY_IN_SECONDS);
            subscribeLP(queue);
        } else {
            boolean notSubscribedYet = !destinationSpecOptional.get()
                    .getSubscribers()
                    .stream()
                    .anyMatch(spec -> spec.getName().equals(SubscriberTranslationKeys.LOADPROFILE_SUBSCRIBER.getKey()));
            if (notSubscribedYet) {
                subscribeLP(destinationSpecOptional.get());
            }
        }
    }

    private void subscribeLP(DestinationSpec queue) {
        queue.activate();
        queue.subscribe(SubscriberTranslationKeys.LOADPROFILE_SUBSCRIBER, DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN);
    }
}
