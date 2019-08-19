/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.servicecall.ServiceCallService;

import javax.inject.Inject;

public class Upgrader extends AbstractInstaller implements com.elster.jupiter.upgrade.Upgrader {

    @Inject
    public Upgrader(ServiceCallService serviceCallService, CustomPropertySetService customPropertySetService,
            MessageService messageService) {
        super(serviceCallService, customPropertySetService, messageService);
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        createServiceCallTypes();
    }
}
