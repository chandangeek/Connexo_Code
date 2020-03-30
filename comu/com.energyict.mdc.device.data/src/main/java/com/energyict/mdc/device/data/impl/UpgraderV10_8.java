/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskProperty;
import com.energyict.mdc.device.data.impl.crlrequest.CrlRequestTaskPropertyImpl;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.time.Clock;
import java.util.Arrays;

public class UpgraderV10_8 implements Upgrader {

    private static final long PARTITIONSIZE = 86400L * 30L * 1000L;
    private final DataModel dataModel;
    private final Clock clock;

    @Inject
    UpgraderV10_8(DataModel dataModel, Clock clock) {
        this.dataModel = dataModel;
        this.clock = clock;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        boolean upgradeCRLneeded = containsCRLColumn();
        if (upgradeCRLneeded) {
            updateCRLTable();
        }
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 8));
        updateCRLTable();
        addAutoIncrementPartitions();
    }

    private void updateCRLTable() {
        dataModel.mapper(CrlRequestTaskProperty.class).find().forEach(CrlRequestTaskProperty::delete);
        execute(dataModel, "ALTER TABLE " + TableSpecs.DDC_CRL_REQUEST_TASK_PROPS.name() + " DROP COLUMN SECURITY_ACCESSOR");
    }

    private boolean containsCRLColumn() {
        return executeQuery(dataModel, "SELECT distinct column_name from all_tab_columns where TABLE_NAME = '" + TableSpecs.DDC_CRL_REQUEST_TASK_PROPS.name()
                + "' AND column_name = '" + CrlRequestTaskPropertyImpl.Fields.CRL_SIGNER.name() + "';", ResultSet::next);
    }

    private void addAutoIncrementPartitions() {
        //append partition for next month and enable auto increment partition interval
        if (dataModel.getSqlDialect().hasPartitioning()) {
            Arrays.asList("DDC_COMSESSION", "DDC_COMTASKEXECSESSION").forEach(tableName ->
                    execute(dataModel, "LOCK TABLE " + tableName + " PARTITION FOR (" + clock.instant().plusMillis(PARTITIONSIZE).toEpochMilli() + ") IN SHARE MODE",
                            "ALTER TABLE " + tableName + " SET INTERVAL (" + PARTITIONSIZE + ")")
            );
        }
    }
}
