/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.metering.impl.upgraders;

import com.elster.jupiter.metering.impl.DefaultDeviceEventTypesInstaller;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.logging.Logger;

public class UpgraderV10_9_19 implements Upgrader {
    private final DataModel dataModel;

    @Inject
    public UpgraderV10_9_19(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        execute(dataModel,
                "alter table DAL_HIST_ALM_RELATED_EVT drop constraint VAL_FK_HSTALM_REL_EVTSEVT",
                "drop index VAL_FK_HSTALM_REL_EVTSEVT",
                "alter table DAL_OPEN_ALM_RELATED_EVT drop constraint VAL_FK_OPNALM_REL_EVTSEVT",
                "drop index VAL_FK_OPNALM_REL_EVTSEVT",
                "create table MTR_ENDDEVICEEVENTDETAIL_TMP as (select * from MTR_ENDDEVICEEVENTDETAIL)",
                "drop table MTR_ENDDEVICEEVENTDETAIL cascade constraints",
                "drop index PK_MTR_ENDDEVICEEVENTDETAIL",
                "rename MTR_ENDDEVICEEVENTDETAIL_TMP to MTR_ENDDEVICEEVENTDETAIL"
        );
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 9, 19));
    }
}
