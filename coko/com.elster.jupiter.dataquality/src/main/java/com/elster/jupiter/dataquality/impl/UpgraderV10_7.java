/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.dataquality.impl.calc.DataQualityKpiCalculatorHandlerFactory;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import com.google.inject.Inject;

import java.util.Optional;
import java.util.logging.Logger;

public class UpgraderV10_7  implements Upgrader {
    private final DataModel dataModel;
    private final MessageService messageService;
    private final AppService appService;
    private final Installer installer;
    private final Logger logger;

    @Inject
    UpgraderV10_7(DataModel dataModel, MessageService messageService, AppService appService, Installer installer) {
        this.dataModel = dataModel;
        this.messageService = messageService;
        this.appService = appService;
        this.installer = installer;
        this.logger = Logger.getLogger(this.getClass().getName());
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 7));
        deleteOldDestination();
        installer.createMessageHandlers();
    }

    private void deleteOldDestination() {
        Optional<DestinationSpec> destinationSpec = messageService.getDestinationSpec(DataQualityKpiCalculatorHandlerFactory.TASK_DESTINATION);
        destinationSpec.ifPresent(destination -> {
            destination.unSubscribe(DataQualityKpiCalculatorHandlerFactory.TASK_DESTINATION);
            destination.delete();
        });
    }
}
