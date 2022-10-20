/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.metering.impl.upgraders;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;

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
                "rename MTR_ENDDEVICEEVENTDETAIL_TMP to MTR_ENDDEVICEEVENTDETAIL",
                "alter table MTR_ENDDEVICEEVENTDETAIL add (LOGBOOKID number default 0 not null)",
                "update MTR_ENDDEVICEEVENTDETAIL set LOGBOOKID = (\n" +
                        "select LOGBOOKID from MTR_ENDDEVICEEVENTRECORD where \n" +
                        "MTR_ENDDEVICEEVENTDETAIL.ENDDEVICEID = MTR_ENDDEVICEEVENTRECORD.ENDDEVICEID and\n" +
                        "MTR_ENDDEVICEEVENTDETAIL.EVENTTYPE = MTR_ENDDEVICEEVENTRECORD.EVENTTYPE and\n" +
                        "MTR_ENDDEVICEEVENTDETAIL.CREATEDDATETIME = MTR_ENDDEVICEEVENTRECORD.CREATEDDATETIME)"
        );
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 9, 19));
    }
}
