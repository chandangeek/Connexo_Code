/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

public class UpgraderV10_7_1 implements Upgrader {

    private final DataModel dataModel;


    @Inject
    public UpgraderV10_7_1(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 7, 1));
        updatePrimaryKeyIndex();
    }

    private void updatePrimaryKeyIndex() {
        String sqlStatement = "ALTER INDEX PK_DDC_COMTASKEXECSESSION REBUILD REVERSE";
        try (Connection connection = dataModel.getConnection(true)) {
            try (PreparedStatement statement = connection.prepareStatement(sqlStatement)) {
                Logger.getAnonymousLogger().info("Executing: " + sqlStatement);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new UnderlyingSQLFailedException(e);
            }
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

}
