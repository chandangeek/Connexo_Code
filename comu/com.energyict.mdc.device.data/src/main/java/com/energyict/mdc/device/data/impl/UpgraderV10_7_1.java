/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.OrmService;
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
    private final OrmService ormService;


    @Inject
    public UpgraderV10_7_1(DataModel dataModel, OrmService ormService) {
        this.dataModel = dataModel;
        this.ormService = ormService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 7, 1));
        if (ormService.isTest()) {
            return;
        }
        executeStatement("ALTER INDEX PK_DDC_COMTASKEXECSESSION REBUILD REVERSE");
        executeStatement("ALTER INDEX FK_DDC_COMTASKJENTRY_SESSION REBUILD REVERSE");
    }

    private void executeStatement(String sqlStatement){
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
