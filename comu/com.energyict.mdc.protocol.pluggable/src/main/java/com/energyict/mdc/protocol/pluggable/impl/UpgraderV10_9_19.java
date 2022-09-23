/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.elster.jupiter.orm.Version.version;

/**
 * Upgrades the database to version 10.2.
 */
public class UpgraderV10_9_19 implements Upgrader {
    private final DataModel dataModel;

    @Inject
    UpgraderV10_9_19(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, version(10, 9, 19));
        this.upgradeProtocolsCapabilities();
    }

    private void upgradeProtocolsCapabilities() {
        try (Connection connection = this.dataModel.getConnection(true)) {
            try (PreparedStatement statement = this.upgradeProtocolsCapabilitiesStatement(connection)) {
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new UnderlyingSQLFailedException(e);
            }
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private PreparedStatement upgradeProtocolsCapabilitiesStatement(Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "UPDATE PPC_CAPABILITIESADAPTERMAPPING SET DEVICEPROTOCOLCAPABILITIES = ? WHERE DEVICEPROTOCOLJAVACLASSNAME = ?");
        statement.setInt(1, 0x03);
        statement.setString(2, "com.energyict.protocolimpl.mbus.generic.Generic");
        return statement;
    }
}