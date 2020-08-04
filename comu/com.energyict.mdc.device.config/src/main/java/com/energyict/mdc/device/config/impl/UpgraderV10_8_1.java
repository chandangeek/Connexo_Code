/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.DuplicateSubscriberNameException;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import com.energyict.mdc.device.config.DeviceConfigurationService;

import javax.inject.Inject;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static com.elster.jupiter.messaging.DestinationSpec.whereCorrelationId;

@LiteralSql
class UpgraderV10_8_1 implements Upgrader {
    private static final Logger logger = Logger.getLogger(UpgraderV10_8_1.class.getName());
    private final DataModel dataModel;
    private final MessageService messageService;

    @Inject
    UpgraderV10_8_1(DataModel dataModel, MessageService messageService, MessageService messageService1) {
        this.dataModel = dataModel;
        this.messageService = messageService1;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 8, 1));
        fixDeviceTypeChangesEventHandler();
    }

    private void fixDeviceTypeChangesEventHandler() {
        Optional<DestinationSpec> destinationSpec = messageService.getDestinationSpec(EventService.JUPITER_EVENTS);
        if (destinationSpec.isPresent()) {
            List<SubscriberSpec> subscribers = destinationSpec.get().getSubscribers();
            Optional<SubscriberSpec> deviceTypeChangesSubscriber = subscribers.stream().filter(sub -> TranslationKeys.DEVICE_TYPES_CHANGES_EVENT_SUBSC.getKey().equals(sub.getName())).findFirst();
            if (deviceTypeChangesSubscriber.isPresent()) {
                logger.info("Subscriber " + TranslationKeys.DEVICE_TYPES_CHANGES_EVENT_SUBSC.getKey() + " found; unsubscribe");
                try {
                    destinationSpec.get().unSubscribe(TranslationKeys.DEVICE_TYPES_CHANGES_EVENT_SUBSC.getKey());
                } catch (UnderlyingSQLFailedException e) {
                    logger.severe("Error while trying to unsubscribe: " + e);
                }
            } else {
                logger.info("Subscriber " + TranslationKeys.DEVICE_TYPES_CHANGES_EVENT_SUBSC.getKey() + " not found for " + destinationSpec.get().getName());
            }
            try {
                logger.info("Adding subscriber " + TranslationKeys.DEVICE_TYPES_CHANGES_EVENT_SUBSC.getKey() + " for " + destinationSpec.get().getName());
                destinationSpec.get().subscribe(
                        TranslationKeys.DEVICE_TYPES_CHANGES_EVENT_SUBSC,
                        DeviceConfigurationService.COMPONENTNAME,
                        Layer.DOMAIN,
                        whereCorrelationId().isEqualTo("com/energyict/mdc/device/config/devicetype/CREATED")
                                .or(whereCorrelationId().isEqualTo("com/energyict/mdc/device/config/devicetype/DELETED"))
                                .or(whereCorrelationId().isEqualTo("com/energyict/mdc/device/config/devicetype/dlc/UPDATED"))
                                .or(whereCorrelationId().isEqualTo("com/energyict/mdc/device/lifecycle/config/dlc/update"))
                                .or(whereCorrelationId().isEqualTo("com/elster/jupiter/fsm/UPDATED")));
            } catch (DuplicateSubscriberNameException e) {
                // subscriber already exists, ignoring
                logger.warning("DuplicateSubscriberNameException during DTC upgrade: " + e);
            }
        }
    }
}
