/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl;

import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.upgrade.Upgrader;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallTypes;

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
