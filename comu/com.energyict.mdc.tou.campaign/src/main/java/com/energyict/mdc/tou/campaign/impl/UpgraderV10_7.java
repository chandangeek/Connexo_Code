/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.impl;

import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UpgraderV10_7 implements com.elster.jupiter.upgrade.Upgrader {

    private final ServiceCallService serviceCallService;

    @Inject
    public UpgraderV10_7(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        for (ServiceCallTypes serviceCallType : ServiceCallTypes.values()) {
            serviceCallService.findServiceCallType(serviceCallType.getTypeName(), serviceCallType.getTypeVersion()).ifPresent(ServiceCallType::save);
        }
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
