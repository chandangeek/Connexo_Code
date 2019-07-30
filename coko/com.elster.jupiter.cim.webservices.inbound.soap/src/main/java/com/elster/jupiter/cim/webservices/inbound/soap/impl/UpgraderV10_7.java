/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.ServiceCallCommands;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UpgraderV10_7 extends AbstractInstaller implements com.elster.jupiter.upgrade.Upgrader {

    private final Logger logger = Logger.getLogger("upgrade");

    @Inject
    public UpgraderV10_7(ServiceCallService serviceCallService, CustomPropertySetService customPropertySetService,
                         MessageService messageService) {
        super(serviceCallService, customPropertySetService, messageService);
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        doTry("Create appKey for service call types", this::updateServiceCallTypes, logger);
    }

   private void updateServiceCallTypes() {
       for (ServiceCallCommands.ServiceCallTypes type : ServiceCallCommands.ServiceCallTypes.values()) {
           type.getApplication().ifPresent(
                   application ->
                           serviceCallService
                                   .findServiceCallType(type.getTypeName(), type.getTypeVersion()).ifPresent(
                                   serviceCallType -> {
                                       serviceCallType.setApplication(application);
                                       serviceCallType.save();
                                   }
                           ));
       }
   }

    private void doTry(String description, Runnable runnable, Logger logger) {
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
