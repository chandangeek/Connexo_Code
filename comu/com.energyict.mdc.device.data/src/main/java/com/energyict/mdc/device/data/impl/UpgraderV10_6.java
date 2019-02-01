/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.metering.zone.MeteringZoneService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;

import com.energyict.mdc.device.data.DeviceDataServices;

import javax.inject.Inject;
import java.util.Optional;

public class UpgraderV10_6 implements Upgrader {

    private final DataModel dataModel;
    private final UserService userService;
    private final MessageService messageService;
    private final PrivilegesProviderV10_6 privilegesProviderV10_6;

    @Inject
    public UpgraderV10_6(DataModel dataModel, UserService userService, MessageService messageService, PrivilegesProviderV10_6 privilegesProviderV10_6) {
        this.dataModel = dataModel;
        this.userService = userService;
        this.messageService = messageService;
        this.privilegesProviderV10_6 = privilegesProviderV10_6;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 6));
        createMessageHandler();
        userService.addModulePrivileges(privilegesProviderV10_6);
    }

    private void createMessageHandler() {
        QueueTableSpec defaultQueueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        Optional<DestinationSpec> destinationSpecOptional = messageService.getDestinationSpec(MeteringZoneService.BULK_ZONE_QUEUE_DESTINATION);
        if (!destinationSpecOptional.isPresent()) {
            DestinationSpec queue = defaultQueueTableSpec.createDestinationSpec(MeteringZoneService.BULK_ZONE_QUEUE_DESTINATION, Installer.DEFAULT_RETRY_DELAY_IN_SECONDS);
            subscribe(queue);
        } else {
            boolean notSubscribedYet = !destinationSpecOptional.get()
                    .getSubscribers()
                    .stream()
                    .anyMatch(spec -> spec.getName().equals(SubscriberTranslationKeys.ZONE_SUBSCRIBER.getKey()));
            if (notSubscribedYet) {
                subscribe(destinationSpecOptional.get());
            }
        }
    }

    private void subscribe(DestinationSpec queue) {
        queue.activate();
        queue.subscribe(SubscriberTranslationKeys.ZONE_SUBSCRIBER, DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN);
    }

}