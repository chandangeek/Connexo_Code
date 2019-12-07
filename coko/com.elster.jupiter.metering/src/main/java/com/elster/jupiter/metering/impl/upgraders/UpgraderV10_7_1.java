/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.metering.impl.upgraders;

import com.elster.jupiter.metering.impl.DefaultDeviceEventTypesInstaller;
import com.elster.jupiter.metering.impl.InstallerV10_7_1Impl;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.util.logging.Logger;

public class UpgraderV10_7_1 implements Upgrader {
    private final DataModel dataModel;
    private final Logger logger;
    private final InstallerV10_7_1Impl installerV10_7_1;
    private final DefaultDeviceEventTypesInstaller defaultDeviceEventTypesInstaller;

    @Inject
    public UpgraderV10_7_1(DataModel dataModel, Logger logger, InstallerV10_7_1Impl installerV10_7_1, DefaultDeviceEventTypesInstaller defaultDeviceEventTypesInstaller) {
        this.dataModel = dataModel;
        this.logger = logger;
        this.installerV10_7_1 = installerV10_7_1;
        this.defaultDeviceEventTypesInstaller = defaultDeviceEventTypesInstaller;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 7, 1));
        installerV10_7_1.install(dataModelUpgrader, logger);
        defaultDeviceEventTypesInstaller.installIfNotPresent(logger);
        execute(dataModel,
                "update MTR_READINGTYPE set ID = MTR_READINGTYPEID.NEXTVAL",
                "alter table MTR_READINGTYPE add constraint UK_MTR_READINGTYPE_ID unique (ID)",
                "merge into MTR_READINGQUALITY rq"
                        + " using (select ID, MRID from MTR_READINGTYPE) rt"
                        + " on (rq.READINGTYPE = rt.MRID)"
                        + " when matched then update set rq.READINGTYPEID = rt.ID",
                "alter table MTR_READINGQUALITY add constraint PK_MTR_READINGQUALITY primary key (CHANNELID, READINGTIMESTAMP, TYPE, READINGTYPEID)"
                // TODO: uncomment
//                "alter table MTR_READINGQUALITY drop column READINGTYPE",
//                "drop sequence MTR_READINGQUALITYID",
//                "alter table MTR_READINGQUALITY drop column ID");
        );
    }
}
