/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.database;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;

import static com.elster.jupiter.orm.Version.version;

@LiteralSql
public class UpgraderV10_7 implements Upgrader {

    private final DataModel dataModel;


    @Inject
    UpgraderV10_7(DataModel dataModel) {
        this.dataModel = dataModel;

    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, version(10, 7));
    }

}
