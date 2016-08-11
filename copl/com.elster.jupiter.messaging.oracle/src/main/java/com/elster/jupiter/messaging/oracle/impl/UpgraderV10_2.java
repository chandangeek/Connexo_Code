/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging.oracle.impl;

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
 * Upgrades the database schema of this bundle to version 10.2.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-08-10 (16:21)
 */
public class UpgraderV10_2 implements Upgrader {

    private final DataModel dataModel;

    @Inject
    public UpgraderV10_2(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(this.dataModel, version(10, 2));
        try (Connection connection = this.dataModel.getConnection(true)) {
            this.migrate(connection);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private void migrate(Connection connection) throws SQLException {
        new AddDequeueSleepAndWaitSeconds(connection).migrate();
    }

    private static class AddDequeueSleepAndWaitSeconds {
        private final Connection connection;

        private AddDequeueSleepAndWaitSeconds(Connection connection) {
            this.connection = connection;
        }

        void migrate() throws SQLException {
            this.updateDequeueField("DEQUEUE_WAIT_SECS", 60, 1);
            this.updateDequeueField("DEQUEUE_SLEEP_SECS", 0, 10);
        }

        private void updateDequeueField(String fieldName, int maxDequeueSecondsForSystemDefined, int maxDequeueSeconds) throws SQLException {
            this.updateDequeueField(fieldName, maxDequeueSecondsForSystemDefined, true);
            this.updateDequeueField(fieldName, maxDequeueSeconds, false);
        }

        private void updateDequeueField(String fieldName, int seconds, boolean systemManaged) throws SQLException {
            try (PreparedStatement updateStatement = this.connection.prepareStatement("UPDATE MSG_SUBSCRIBERSPEC SET " + fieldName + " = ? WHERE SYSTEMMANAGED = ?")) {
                updateStatement.setInt(1, seconds);
                updateStatement.setString(2, systemManaged ? "Y" : "N");
                updateStatement.executeUpdate();
            }
        }
    }

}