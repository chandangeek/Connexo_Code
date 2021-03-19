package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;
import com.energyict.mdc.device.data.impl.ami.servicecall.ServiceCallCommands;

import javax.inject.Inject;

public class UpgraderV10_4_2 implements Upgrader {

    private final DataModel dataModel;
    private final InstallerV10_2Impl installerV10_2;

    @Inject
    public UpgraderV10_4_2(DataModel dataModel, InstallerV10_2Impl installerV10_2) {
        this.dataModel = dataModel;
        this.installerV10_2 = installerV10_2;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 4, 2));
        createServiceCallTypes();
    }

    private void createServiceCallTypes() {
        for (ServiceCallCommands.ServiceCallTypeMapping serviceCallTypeMapping : ServiceCallCommands.ServiceCallTypeMapping.values()) {
            installerV10_2.createServiceCallTypeIfNotPresent(serviceCallTypeMapping);
        }
    }
}
