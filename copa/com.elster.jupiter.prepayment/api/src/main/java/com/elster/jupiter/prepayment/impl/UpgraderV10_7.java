/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.prepayment.impl;

import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.prepayment.impl.servicecall.ServiceCallCommands.ServiceCallTypes;

import javax.inject.Inject;

public class UpgraderV10_7 implements Upgrader {

    private final ServiceCallService serviceCallService;

    @Inject
    public UpgraderV10_7(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        updateServiceCallTypes();
    }

    private void updateServiceCallTypes() {
        for (ServiceCallTypes type : ServiceCallTypes.values()) {
            serviceCallService
                    .findServiceCallType(type.getTypeName(), type.getTypeVersion()).ifPresent(
                    serviceCallType -> {
                        serviceCallType.setApplication( type.getApplication());
                        serviceCallType.save();
                    }
            );
        }
    }

}
