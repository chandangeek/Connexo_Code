/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

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
    private final InstallerImpl installer;

    @Inject
    UpgraderV10_7(DataModel dataModel, MessageService messageService, InstallerImpl installer) {
        this.dataModel = dataModel;
        this.messageService = messageService;
        this.installer = installer;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 7));
        deleteOldDestinations();
        installer.createMessageHandlers();
    }

    private void deleteOldDestinations() {
        Optional<DestinationSpec> destinationSpec = messageService.getDestinationSpec(ValidationServiceImpl.DESTINATION_NAME);
        destinationSpec.ifPresent(destination -> {
            destination.unSubscribe(ValidationServiceImpl.DESTINATION_NAME);
            destination.delete();
        });
    }
}
