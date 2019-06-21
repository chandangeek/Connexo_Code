/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import com.energyict.mdc.device.data.impl.kpi.DataCollectionKpiCalculatorHandlerFactory;
import com.energyict.mdc.device.data.impl.pki.tasks.crlrequest.CrlRequestHandlerFactory;

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
        deleteOldDestinations();
        installer.createPrioritizedMessageHandlers();
    }

    private void deleteOldDestinations() {
        Optional<DestinationSpec> destinationSpec = messageService.getDestinationSpec(CrlRequestHandlerFactory.CRL_REQUEST_TASK_DESTINATION_NAME);
        destinationSpec.ifPresent(destination -> {
            destination.unSubscribe(CrlRequestHandlerFactory.CRL_REQUEST_TASK_DESTINATION_NAME);
            destination.delete();
        });
        destinationSpec = messageService.getDestinationSpec(DataCollectionKpiCalculatorHandlerFactory.TASK_DESTINATION);
        destinationSpec.ifPresent(destination -> {
            destination.unSubscribe(DataCollectionKpiCalculatorHandlerFactory.TASK_DESTINATION);
            destination.delete();
        });
    }
}
