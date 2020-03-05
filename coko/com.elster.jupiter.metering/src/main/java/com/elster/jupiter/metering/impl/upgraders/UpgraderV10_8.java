package com.elster.jupiter.metering.impl.upgraders;

import com.elster.jupiter.metering.impl.DefaultDeviceEventTypesInstaller;
import com.elster.jupiter.metering.impl.EndDeviceControlTypeInstallerUtil;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.util.logging.Logger;

public class UpgraderV10_8 implements Upgrader {
    private final DataModel dataModel;
    private final ServerMeteringService meteringService;
    private final DefaultDeviceEventTypesInstaller defaultDeviceEventTypesInstaller;
    private final Logger logger;

    @Inject
    public UpgraderV10_8(DataModel dataModel, ServerMeteringService meteringService, DefaultDeviceEventTypesInstaller defaultDeviceEventTypesInstaller) {
        this.dataModel = dataModel;
        this.meteringService = meteringService;
        this.defaultDeviceEventTypesInstaller = defaultDeviceEventTypesInstaller;
        this.logger = Logger.getLogger(this.getClass().getName());
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 8));
        new EndDeviceControlTypeInstallerUtil(meteringService).createEndDeviceControlTypes(logger);
        defaultDeviceEventTypesInstaller.installIfNotPresent(logger);
    }
}
