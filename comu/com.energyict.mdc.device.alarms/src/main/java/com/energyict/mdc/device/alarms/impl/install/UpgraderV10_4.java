/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.impl.install;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.DuplicateSubscriberNameException;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.impl.i18n.TranslationKeys;

import javax.inject.Inject;

import static com.elster.jupiter.messaging.DestinationSpec.whereCorrelationId;

public class UpgraderV10_4 implements Upgrader {
    private final MessageService messageService;

    @Inject
    public UpgraderV10_4(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        setAQSubscriber();
    }

    private void setAQSubscriber() {
        DestinationSpec destinationSpec = messageService.getDestinationSpec(EventService.JUPITER_EVENTS).get();
        try {
            destinationSpec.subscribe(
                    TranslationKeys.DEVICE_TYPES_CHANGES_EVENT_SUBSC,
                    DeviceAlarmService.COMPONENT_NAME,
                    Layer.DOMAIN,
                    whereCorrelationId().isEqualTo("com/energyict/mdc/device/config/devicetype/CREATED")
                            .or(whereCorrelationId().isEqualTo("com/elster/jupiter/metering/enddeviceevent/CREATED"))
                            .or(whereCorrelationId().isEqualTo("com/energyict/mdc/device/config/devicetype/DELETED"))
                            .or(whereCorrelationId().isEqualTo("com/energyict/mdc/device/config/devicetype/dlc/UPDATED"))
                            .or(whereCorrelationId().isEqualTo("com/energyict/mdc/device/lifecycle/config/dlc/update"))
                            .or(whereCorrelationId().isEqualTo("com/elster/jupiter/fsm/UPDATED")));
        } catch (DuplicateSubscriberNameException e) {
            // subscriber already exists, ignoring
        }
    }

}
