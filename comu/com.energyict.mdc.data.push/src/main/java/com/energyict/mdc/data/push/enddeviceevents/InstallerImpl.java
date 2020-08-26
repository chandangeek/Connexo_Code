/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.data.push.enddeviceevents;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.util.logging.Logger;

import static com.elster.jupiter.messaging.DestinationSpec.whereCorrelationId;

public class InstallerImpl implements FullInstaller {
    private final MessageService messageService;

    @Inject
    public InstallerImpl(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        addJupiterEventSubscriber();
    }

    private void addJupiterEventSubscriber() {
        messageService.getDestinationSpec(EventService.JUPITER_EVENTS).ifPresent(destinationSpec -> {
            if (destinationSpec.getSubscribers().stream()
                    .map(SubscriberSpec::getName)
                    .noneMatch(DataPushTranslationKey.EVENT_SUBSCRIBER.getKey()::equals)) {
                destinationSpec.subscribe(
                        DataPushTranslationKey.EVENT_SUBSCRIBER,
                        EndDeviceEventMessageHandlerFactory.COMPONENT_NAME,
                        Layer.DOMAIN,
                        whereCorrelationId().isEqualTo(EventType.END_DEVICE_EVENT_CREATED.topic()));
            }
        });
    }
}
