/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

class UpgraderV10_3 implements Upgrader {

    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final DataModel dataModel;

    String name = this.getClass().getName();

    @Inject
    UpgraderV10_3(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 3));
        initiateSimultaneousConnectionsForPartialConnectionTasks();
        moveProtocolDialectProperties();
        migrateDialectNames();
    }

    // Initiate Partial Connection Tasks: set simultaneousconnections to 1
    private void initiateSimultaneousConnectionsForPartialConnectionTasks() {
        logger.fine("Updating partial connection tasks");
        List<String> sql = new ArrayList<>();
        sql.add("UPDATE DTC_PARTIALCONNECTIONTASK SET simultaneousconnections=1 where simultaneousconnections = 0");
        dataModel.useConnectionRequiringTransaction(connection -> {
            try (Statement statement = connection.createStatement()) {
                sql.forEach(sqlCommand -> execute(statement, sqlCommand));
            }
        });
    }

    // Migrate the old names of the dialects to the new names (due to UPL)
    private void migrateDialectNames() {
        logger.fine("Migrating dialect names (due to universal protocol layer feature)");
        List<String> sql = new ArrayList<>();
        sql.add("UPDATE DTC_DIALECTCONFIGPROPERTIES SET NAME = 'SerialDlmsDialect' WHERE NAME = 'SerialDialect'");
        sql.add("UPDATE DTC_DIALECTCONFIGPROPERTIES SET DEVICEPROTOCOLDIALECT = 'SerialDlmsDialect' WHERE DEVICEPROTOCOLDIALECT = 'SerialDialect'");
        sql.add("UPDATE DTC_DIALECTCONFIGPROPERTIES SET NAME = 'TcpDlmsDialect' WHERE NAME = 'TcpDialect'");
        sql.add("UPDATE DTC_DIALECTCONFIGPROPERTIES SET DEVICEPROTOCOLDIALECT = 'TcpDlmsDialect' WHERE DEVICEPROTOCOLDIALECT = 'TcpDialect'");
        dataModel.useConnectionRequiringTransaction(connection -> {
            try (Statement statement = connection.createStatement()) {
                sql.forEach(sqlCommand -> execute(statement, sqlCommand));
            }
        });
    }

    // Move ProtocolDialectProperties from Communication Task Enablements to Partial Connection Task
    private void moveProtocolDialectProperties() {
        logger.fine("Moving protocol dialect properties from DTC_COMTASKENABLEMENT to DTC_PARTIALCONNECTIONTASK");
        dataModel.useConnectionNotRequiringTransaction(connection -> {
            try (Statement retrieveDialectPropertiesIdStatement = connection.createStatement();
                Statement updatePartialConnectionTaskStatement = connection.createStatement()) {
                String sql = "SELECT DISTINCT DTC_COMTASKENABLEMENT.DIALECTCONFIGPROPERTIES, NVL(PARTIALCONNECTIONTASK, DECODE(USEDEFAULTCONNECTIONTASK, 0, NULL, 1, DTC_PARTIALCONNECTIONTASK.ID )) AS PCTID \n" +
                        "FROM DTC_COMTASKENABLEMENT, DTC_PARTIALCONNECTIONTASK WHERE DTC_COMTASKENABLEMENT.DEVICECOMCONFIG = DTC_PARTIALCONNECTIONTASK.DEVICECONFIG AND DTC_PARTIALCONNECTIONTASK.ISDEFAULT = 1";
                ResultSet rs = retrieveDialectPropertiesIdStatement.executeQuery(sql);
                while (rs.next()) {
                    updatePartialConnectionTaskStatement.addBatch(String.format("UPDATE DTC_PARTIALCONNECTIONTASK SET DIALECTCONFIGPROPERTIES = %1s WHERE ID = %2s", rs.getLong(1), rs.getLong(2)));
                }
                updatePartialConnectionTaskStatement.executeBatch();
                alterDTC_PARTIALCONNECTIONTASK(updatePartialConnectionTaskStatement); // Need to be done afterwards to avoid 'ORA-01758: Tabel moet leeg zijn om verplichte (NOT NULL) kolom toe te kunnen voegen.'
            }
        });
    }

    private void alterDTC_PARTIALCONNECTIONTASK(Statement statement) {
        String sql = "ALTER TABLE DTC_PARTIALCONNECTIONTASK MODIFY (DIALECTCONFIGPROPERTIES NOT NULL)";
        try {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

}
