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
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Jozsef Szekrenyes on 11/22/2018.
 */
public class UpgraderV10_4_5 implements Upgrader {

    private static final Logger logger = Logger.getLogger(UpgraderV10_4_5.class.getName());
    private static final long PARTITIONSIZE = 86400L * 1000L * 30L;
    private static final int NB_MONTHS = 12;

    private final DataModel dataModel;

    @Inject
    public UpgraderV10_4_5(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModel.useConnectionRequiringTransaction(this::createPartitionedTable);
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 4, 5));
        //temporary disable due to fail in migration
        //createUnsubscriberForMessageQueue();
    }

    private void createPartitionedTable(Connection connection) {
        if (!dataModel.getSqlDialect().hasPartitioning()) {
            logger.warning("Partitioning not enabled - won't partition new tables (" +
                    "DDC_COMTASKEXECJOURNALENTRY," +
                    "DDC_COMSESSIONJOURNALENTRY" +
                    ")");

            return;
        }
        List<String> alterSql = Arrays.asList(
                getModifyIntervalPartitionDdl("DDC_COMTASKEXECJOURNALENTRY", "timestamp"),
                getModifyIntervalPartitionDdl("DDC_COMSESSIONJOURNALENTRY", "timestamp"),
                getModifyRangePartitionDdl("DDC_COMTASKEXECSESSION", "stopdate"),
                getModifyRangePartitionDdl("DDC_COMSESSION", "stopdate")
        );
        alterSql.forEach(sqlStatement -> {
            try (PreparedStatement statement = connection.prepareStatement(sqlStatement)) {
                logger.info("Executing: " + sqlStatement);
                statement.executeUpdate();
            } catch (SQLException e) {
                logger.warning("Couldn't perform upgrade for \n" + sqlStatement);
                throw new UnderlyingSQLFailedException(e);
            }
        });
    }

    private String getModifyIntervalPartitionDdl(String table, String partitionColumn) {
        StringBuilder sb = getAlterTablePart(table, partitionColumn);
        sb.append("INTERVAL (");
        sb.append(PARTITIONSIZE);
        sb.append(") ( PARTITION P0 VALUES LESS THAN (0)) ONLINE");

        return sb.toString();
    }

    private String getModifyRangePartitionDdl(String table, String partitionColumn) {
        StringBuilder sb = getAlterTablePart(table, partitionColumn);
        long end = (System.currentTimeMillis() / PARTITIONSIZE) * PARTITIONSIZE;
        String separator = "(";
        for (int i = 0; i < NB_MONTHS; i++) {
            end += PARTITIONSIZE;
            String name = "P" + Instant.ofEpochMilli(end).toString().replaceAll("-", "").substring(0, 8);
            sb.append(separator);
            sb.append(" partition ");
            sb.append(name);
            sb.append(" values less than(");
            sb.append(end);
            sb.append(")");
            separator = ",";
        }
        sb.append(") online");
        return sb.toString();
    }

    private StringBuilder getAlterTablePart(String table, String partitionColumn) {
        StringBuilder sb = new StringBuilder("ALTER TABLE ");
        sb.append(table);
        sb.append(" MODIFY partition by range(");
        sb.append(partitionColumn);
        sb.append(") ");
        return sb;
    }
}
