/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;


public class UpgraderV10_9_26 implements Upgrader {
    private final DataModel dataModel;
    private final Clock clock;
    private static final int CONNECTION_TYPE_PLUGGABLE_CLASS_TYPE = 18;
    private static final String CPC_PLUGGABLECLASS_TABLE_NAME = "CPC_PLUGGABLECLASS";

    @Inject
    public UpgraderV10_9_26(DataModel dataModel, Clock clock) {
        this.dataModel = dataModel;
        this.clock = clock;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 9, 26));
        long upgradeTime = clock.instant().toEpochMilli();
        String sql = "INSERT INTO %s (ID, NAME, JAVACLASSNAME, PLUGGABLETYPE, VERSIONCOUNT, CREATETIME, MODTIME) " +
                "VALUES (?, 'OutboundWebServiceConnectionType', 'com.energyict.mdc.channels.ip.socket.OutboundWebServiceConnectionType'," +
                CONNECTION_TYPE_PLUGGABLE_CLASS_TYPE + ", 1, " + upgradeTime + ", " + upgradeTime + ")";

        try (Connection connection = dataModel.getConnection(false)) {
            PreparedStatement insertStatement = connection.prepareStatement(String.format(sql, CPC_PLUGGABLECLASS_TABLE_NAME));
            insertStatement.setLong(1, getNext(connection, CPC_PLUGGABLECLASS_TABLE_NAME + "ID"));
            insertStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Unable to execute native sql command: " + e.getMessage());
        }
    }

    private long getNext(Connection connection, String sequence) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("select " + sequence + ".nextval from dual")) {
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }
}
