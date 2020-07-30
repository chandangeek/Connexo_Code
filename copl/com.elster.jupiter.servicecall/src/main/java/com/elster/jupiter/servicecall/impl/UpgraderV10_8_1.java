/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.text.DecimalFormat;

public class UpgraderV10_8_1 implements Upgrader {

    private final DataModel dataModel;

    @Inject
    UpgraderV10_8_1(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        execute(dataModel, "ALTER TABLE " + TableSpecs.SCS_SERVICE_CALL + " MODIFY REFERENCE VARCHAR2(80 CHAR)" +
                " GENERATED ALWAYS AS (CASE when id < " + new DecimalFormat("#").format(Math.pow(10, ServiceCallImpl.ZEROFILL_SIZE)) + " THEN " +
                "'SC_'||" + dataModel.getSqlDialect().leftPad("ID", ServiceCallImpl.ZEROFILL_SIZE, "0") + ")" +
                " ELSE 'SC_'||TO_CHAR(ID) END)");
    }
}
