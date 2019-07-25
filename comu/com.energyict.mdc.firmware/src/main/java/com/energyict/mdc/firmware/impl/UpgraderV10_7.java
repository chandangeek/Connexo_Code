/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.upgrade.Upgrader;
import com.energyict.mdc.firmware.impl.campaign.ServiceCallTypes;

import javax.inject.Inject;

import static com.elster.jupiter.orm.Version.version;

public class UpgraderV10_7 implements Upgrader {

    private final DataModel dataModel;
    private final FirmwareCampaignServiceCallLifeCycleInstaller firmwareCampaignServiceCallLifeCycleInstaller;
    private final ServiceCallService serviceCallService;

    @Inject
    UpgraderV10_7(DataModel dataModel, ServiceCallService serviceCallService, FirmwareCampaignServiceCallLifeCycleInstaller firmwareCampaignServiceCallLifeCycleInstaller) {
        this.dataModel = dataModel;
        this.firmwareCampaignServiceCallLifeCycleInstaller = firmwareCampaignServiceCallLifeCycleInstaller;
        this.serviceCallService = serviceCallService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        firmwareCampaignServiceCallLifeCycleInstaller.createServiceCallTypes();
        updateServiceCallTypes();
        dataModelUpgrader.upgrade(dataModel, version(10, 7));
    }

    private void updateServiceCallTypes() {
        for (ServiceCallTypes type : ServiceCallTypes.values()) {
            serviceCallService
                    .findServiceCallType(type.getTypeName(), type.getTypeVersion()).ifPresent(
                    serviceCallType -> {
                        serviceCallType.setApplication(type.getApplication().orElse(null));
                        serviceCallType.setRetryState(type.getRetryState().orElse(null));
                    }
            );
        }
    }
}
