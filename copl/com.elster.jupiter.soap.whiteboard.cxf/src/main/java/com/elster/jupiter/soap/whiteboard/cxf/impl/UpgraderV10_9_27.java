/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;

public class UpgraderV10_9_27 implements Upgrader {

    public static final Version VERSION = Version.version(10, 9, 27);
    private final DataModel dataModel;
    private final OrmService ormService;

    @Inject
    public UpgraderV10_9_27(DataModel dataModel, OrmService ormService) {
        this.dataModel = dataModel;
        this.ormService = ormService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, VERSION);
        execute(dataModel,
                "UPDATE WS_ENDPOINTCFG SET WEBSERVICENAME = 'CIM SendMeterConfig' WHERE WEBSERVICENAME = 'CIM ReplyMeterConfig'");
    }
}
