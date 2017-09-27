/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.impl.database;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.elster.jupiter.orm.Version.version;

public class UpgraderV10_4 implements Upgrader {

    private final DataModel dataModel;

    @Inject
    UpgraderV10_4(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, version(10, 4));
        this.upgradeOpenAlarm();
    }

    private void upgradeOpenAlarm() {
        try (Connection connection = this.dataModel.getConnection(true)) {
            this.upgradeOpenAlarm(connection);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private void upgradeOpenAlarm(Connection connection) {
        String[] sqlStatements = {
                "CREATE OR REPLACE VIEW DAL_ALARM_ALL AS SELECT * FROM DAL_ALARM_OPEN UNION SELECT * FROM DAL_ALARM_HISTORY"};
        for (String sqlStatement : sqlStatements) {
            try (PreparedStatement statement = connection.prepareStatement(sqlStatement)) {
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new UnderlyingSQLFailedException(e);
            }
        }
    }

}
