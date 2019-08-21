/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import com.google.inject.Inject;

import java.util.Optional;

public class UpgraderV10_7 implements Upgrader {
    private final DataModel dataModel;
    private final MessageService messageService;
    private final Installer installer;

    @Inject
    UpgraderV10_7(DataModel dataModel, MessageService messageService, Installer installer) {
        this.dataModel = dataModel;
        this.messageService = messageService;
        this.installer = installer;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 7));
        deleteOldDestination();
        installer.createDestinationAndSubscriber();
    }

    private void deleteOldDestination() {
        Optional<DestinationSpec> destinationSpec = messageService.getDestinationSpec(Installer.DESTINATION_NAME);
        destinationSpec.ifPresent(destination -> {
            destination.unSubscribe(Installer.DESTINATION_NAME);
            destination.delete();
        });
    }
}
