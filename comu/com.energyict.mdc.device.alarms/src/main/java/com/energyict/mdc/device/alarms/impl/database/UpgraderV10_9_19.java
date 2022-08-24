/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.alarms.impl.database;

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
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 9, 19));
        execute(dataModel,
                "update DAL_OPEN_ALM_RELATED_EVT set LOGBOOKID = (\n" +
                        "select LOGBOOKID from MTR_ENDDEVICEEVENTRECORD where \n" +
                        "DAL_OPEN_ALM_RELATED_EVT.ENDDEVICEID = MTR_ENDDEVICEEVENTRECORD.ENDDEVICEID and\n" +
                        "DAL_OPEN_ALM_RELATED_EVT.EVENTTYPE = MTR_ENDDEVICEEVENTRECORD.EVENTTYPE and\n" +
                        "DAL_OPEN_ALM_RELATED_EVT.RECORDTIME = MTR_ENDDEVICEEVENTRECORD.CREATEDDATETIME)",
                "update DAL_HIST_ALM_RELATED_EVT set LOGBOOKID = (\n" +
                        "select LOGBOOKID from MTR_ENDDEVICEEVENTRECORD where \n" +
                        "DAL_HIST_ALM_RELATED_EVT.ENDDEVICEID = MTR_ENDDEVICEEVENTRECORD.ENDDEVICEID and\n" +
                        "DAL_HIST_ALM_RELATED_EVT.EVENTTYPE = MTR_ENDDEVICEEVENTRECORD.EVENTTYPE and\n" +
                        "DAL_HIST_ALM_RELATED_EVT.RECORDTIME = MTR_ENDDEVICEEVENTRECORD.CREATEDDATETIME)"
        );
    }
}
