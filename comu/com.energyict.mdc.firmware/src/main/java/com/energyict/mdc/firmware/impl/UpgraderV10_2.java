/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.upgrade.Upgrader;
import com.energyict.mdc.firmware.FirmwareService;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.elster.jupiter.orm.Version.version;

/**
 * Upgrades the database to version 10.2.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-09-26 (17:03)
 */
public class UpgraderV10_2 implements Upgrader {
    private final DataModel dataModel;

    @Inject
    UpgraderV10_2(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, version(10, 2));
        this.upgradeSubscriberSpecs();
    }

    private void upgradeSubscriberSpecs() {
        try (Connection connection = this.dataModel.getConnection(true)) {
            this.upgradeSubscriberSpecs(connection);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private void upgradeSubscriberSpecs(Connection connection) {
        try (PreparedStatement statement = this.upgradeSubscriberSpecsStatement(connection)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private PreparedStatement upgradeSubscriberSpecsStatement(Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("UPDATE MSG_SUBSCRIBERSPEC SET nls_component = ?, nls_layer = ? WHERE name = ?");
        statement.setString(1, FirmwareService.COMPONENTNAME);
        statement.setString(2, Layer.DOMAIN.name());
        statement.setString(3, TranslationKeys.FIRMWARE_CAMPAIGNS_SUBSCRIBER.getKey());
        return statement;
    }

}