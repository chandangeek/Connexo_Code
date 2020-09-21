/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;

public class UpgraderV10_9 implements Upgrader {

    private final DataModel dataModel;
    private final MessageService messageService;
    private final Installer installer;


    @Inject
    public UpgraderV10_9(DataModel dataModel, MessageService messageService, Installer installer) {
        this.dataModel = dataModel;
        this.messageService = messageService;
        this.installer = installer;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 9));
        installer.createMessageHandlers();
    }
}
