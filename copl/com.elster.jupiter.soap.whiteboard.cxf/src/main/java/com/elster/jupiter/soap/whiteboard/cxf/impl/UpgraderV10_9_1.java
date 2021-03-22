/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;

public class UpgraderV10_9_1 implements Upgrader {
    public static final Version VERSION = Version.version(10, 9, 1);
    private final DataModel dataModel;
    private final OrmService ormService;

    @Inject
    public UpgraderV10_9_1(DataModel dataModel, OrmService ormService) {
        this.dataModel = dataModel;
        this.ormService = ormService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, VERSION);
        if (!ormService.isTest()) {
            execute(dataModel,
                    // TODO: support all this in the default upgrader
                    "alter table WS_OCC_RELATED_ATTR drop constraint WS_UQ_KEY_VALUE",
                    "alter table WS_OCC_RELATED_ATTR add constraint WS_UQ_KEY_VALUE unique (ATTR_KEY, ATTR_VALUE) using index compress 1");
            if (!dataModel.doesIndexExist("WS_CALL_OCCURRENCE", "IX_WS_CALL_START")) {
                execute(dataModel, "create index IX_WS_CALL_START on WS_CALL_OCCURRENCE(STARTTIME desc)");
            }
            if (!dataModel.doesIndexExist("WS_CALL_OCCURRENCE", "IX_WS_CALL_END")) {
                execute(dataModel, "create index IX_WS_CALL_END on WS_CALL_OCCURRENCE(ENDTIME desc)");
            }
            if (!dataModel.doesIndexExist("WS_OCC_RELATED_ATTR", "IX_WS_CALL_ATTR_VALUE")) {
                execute(dataModel, "create index IX_WS_CALL_ATTR_VALUE on WS_OCC_RELATED_ATTR(upper(ATTR_VALUE))");
            }
        }
    }
}
