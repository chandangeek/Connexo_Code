/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;
import com.energyict.mdc.device.data.impl.ami.servicecall.ServiceCallCommands;

import javax.inject.Inject;

public class UpgraderV10_9_1 implements Upgrader {
    private final DataModel dataModel;
    private final EventService eventService;
    private final InstallerV10_2Impl installerV10_2;

    @Inject
    UpgraderV10_9_1(DataModel dataModel,
                    EventService eventService,
                    InstallerV10_2Impl installerV10_2) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.installerV10_2 = installerV10_2;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 9, 1));
        EventType.CREDIT_AMOUNT_CREATED.createIfNotExists(eventService);
        EventType.CREDIT_AMOUNT_UPDATED.createIfNotExists(eventService);
        installerV10_2.createServiceCallTypeIfNotPresent(ServiceCallCommands.ServiceCallTypeMapping.updateCreditAmount);
    }
}
