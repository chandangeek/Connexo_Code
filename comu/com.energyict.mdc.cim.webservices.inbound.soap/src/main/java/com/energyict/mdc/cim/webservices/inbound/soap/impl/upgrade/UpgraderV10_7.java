
/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl.upgrade;

import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.ServiceCallCommands;

import javax.inject.Inject;


public class UpgraderV10_7 implements com.elster.jupiter.upgrade.Upgrader {

    private final ServiceCallService serviceCallService;

    @Inject
    UpgraderV10_7(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        updateServiceCallTypes();
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
}

