/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;



import javax.inject.Inject;
import java.sql.Statement;
import java.time.Clock;


public class UpgraderV10_9_25  implements Upgrader {
    private final DataModel dataModel;
    private final Clock clock;

    @Inject
    public UpgraderV10_9_25(DataModel dataModel, Clock clock) {
        this.dataModel = dataModel;
        this.clock = clock;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 9, 25));
        long upgradeTime = clock.instant().toEpochMilli();
        dataModel.useConnectionRequiringTransaction(connection -> {
            String sql = "INSERT INTO CPC_PLUGGABLECLASS (ID, NAME, JAVACLASSNAME, PLUGGABLETYPE, VERSIONCOUNT, CREATETIME, MODTIME) " +
                    "VALUES (1005, 'OutboundWebServiceConnectionType', 'com.energyict.mdc.channels.ip.socket.OutboundWebServiceConnectionType', 18, 1, " + upgradeTime + ", " + upgradeTime + ")";
            try (Statement statement = connection.createStatement()) {
                execute(statement, sql);
            }
        });
    }
}
