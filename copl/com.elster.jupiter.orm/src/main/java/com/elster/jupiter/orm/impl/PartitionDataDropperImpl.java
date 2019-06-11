/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.DataDropper;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;

import java.sql.*;
import java.time.Instant;
import java.util.logging.Logger;

public class PartitionDataDropperImpl implements DataDropper {

    private final DataModelImpl dataModel;
    private final String tableName;
    private final Logger logger;
    private Instant upTo;


    PartitionDataDropperImpl(DataModelImpl dataModel, String tableName, Logger logger) {
        this.dataModel = dataModel;
        this.tableName = tableName;
        this.logger = logger;
    }

    public void drop(Instant instant) {
        if (!dataModel.getSqlDialect().hasPartitioning()) {
            return;
        }
        this.upTo = instant;
        try {
            dropPartitions();
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    private void dropPartitions() throws SQLException {
        try (Connection connection = dataModel.getConnection(false)) {
            try (PreparedStatement statement = connection.prepareStatement(partitionQuerySql())) {
                statement.setString(1, tableName.toUpperCase());
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String highValueString = resultSet.getString(2);
                        try {
                            long highValue = Long.parseLong(highValueString);
                            if (highValue > 0 && highValue <= upTo.toEpochMilli()) {
                                String partitionName = resultSet.getString(1);
                                dropPartition(tableName, partitionName, connection);
                                logger.info("Dropped partition " + partitionName + " from table " + tableName + " containing entries up to " + Instant.ofEpochMilli(highValue));
                            }
                        } catch (NumberFormatException ex) {
                            // if highValue is not a number , ignore partition
                        }
                    }
                }
            }
        }
    }

    private String partitionQuerySql() {
        return "select partition_name, high_value from user_tab_partitions where table_name = ?";
    }

    private String dropPartitionSql(String tableName, String partitionName) {
        return new StringBuilder("ALTER TABLE ")
                .append(tableName)
                .append(" DROP PARTITION ")
                .append(partitionName)
                .append(" UPDATE GLOBAL INDEXES")
                .toString();
    }


    private void dropPartition(String tableName, String partitionName, Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(dropPartitionSql(tableName, partitionName));
        }
    }

}