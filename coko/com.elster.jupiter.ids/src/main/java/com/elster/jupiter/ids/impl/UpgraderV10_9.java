/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.ids.impl;


import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;

public class UpgraderV10_9 implements Upgrader {

    private final IdsService idsService;
    private final DataModel dataModel;

    @Inject
    UpgraderV10_9(IdsService idsService, DataModel dataModel) {
        this.idsService = idsService;
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        boolean offsetAlreadyExist = dataModel.doesColumnExist("IDS_TIMESERIES", "OFFSET_VALUE");
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 9));
        if (!offsetAlreadyExist)
            execute(dataModel, "UPDATE IDS_TIMESERIES SET OFFSET_VALUE=OFFSET_VALUE*3600");
    }
}
