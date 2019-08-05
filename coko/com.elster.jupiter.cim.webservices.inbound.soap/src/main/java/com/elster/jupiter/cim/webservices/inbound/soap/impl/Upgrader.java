/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.inbound.soap.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.servicecall.ServiceCallService;

public class Upgrader extends AbstractInstaller implements com.elster.jupiter.upgrade.Upgrader {

    private final Logger logger = Logger.getLogger("upgrade");

    @Inject
    public Upgrader(ServiceCallService serviceCallService, CustomPropertySetService customPropertySetService,
            MessageService messageService) {
        super(serviceCallService, customPropertySetService, messageService);
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        doTry("Create service call types", this::createServiceCallTypes, logger);
    }

    void doTry(String description, Runnable runnable, Logger logger) {
        try {
            logger.log(Level.INFO, "Start   : " + description);
            runnable.run();
            logger.log(Level.INFO, "Success : " + description);
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Failed  : " + description, e);
            throw e;
        }
    }
}
