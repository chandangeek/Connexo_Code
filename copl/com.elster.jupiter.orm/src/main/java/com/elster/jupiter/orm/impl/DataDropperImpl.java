/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataDropper;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class DataDropperImpl implements DataDropper {
    private static final long BATCH_SIZE = 5_000L;

    private final TransactionService transactionService;
    private final DataModelImpl dataModel;
    private final String tableName;
    private final Logger logger;
    private Instant upTo;

    private static final BiFunction<DataModelImpl, String, Optional<String>> TABLE_REF_COLUMN = (dataMdl, tblName) ->
            Optional.<TableImpl>ofNullable(dataMdl.getTable(tblName)).<Column>flatMap(TableImpl::partitionColumn).map(Column::getName);


    private static final BiFunction<DataModelImpl, String, Optional<String>> IDS_TABLE_REF_COLUMN = (dataMdl, tblName) -> {
        String referenceColumn = "UTCSTAMP";
        if ("IDS".equals(dataMdl.getName()) && tblName.toUpperCase().startsWith("IDS_VAULT")
                && tblName.split("_").length >= 3) {
            return Optional.of(referenceColumn);
        }
        return Optional.empty();
    };

    private static final BiFunction<DataModelImpl, String, Optional<String>> JOURNAL_TABLE_REF_COLUMN = (dataMdl, tblName) -> {
        String referenceColumn = "JOURNALTIME";
        if (Stream.of("JRNL", "JNRL").anyMatch(suffix -> tblName.toUpperCase().endsWith(suffix))) {
            return Optional.of(referenceColumn);
        }
        return Optional.empty();
    };

    DataDropperImpl(TransactionService transactionService, DataModelImpl dataModel, String tableName, Logger logger) {
        this.transactionService = transactionService;
        this.dataModel = dataModel;
        this.tableName = tableName;
        this.logger = logger;
    }

    public void drop(Instant instant) {
        if (dataModel.getSqlDialect().hasPartitioning()) {
            return;
        }
        this.upTo = instant;
        try {
            deleteRows();
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    private void deleteRows() throws SQLException {
        Optional<String> columnName = getReferenceColumnName();
        if (columnName.isPresent() && hasColumn(columnName.get())) {
            long upToMillis = upTo.toEpochMilli();
            long totalNumberOfRows = getTotalNbOfRowsToDelete(columnName.get(), upToMillis);
            if (totalNumberOfRows > 0) {
                deleteRowsInBatch(columnName.get(), upToMillis, totalNumberOfRows);
            }
        } else {
            logger.warning("Cannot delete rows from table " + tableName + "! No reference column found!");
        }
    }

    private Optional<String> getReferenceColumnName() {
        return Stream.of(TABLE_REF_COLUMN, IDS_TABLE_REF_COLUMN, JOURNAL_TABLE_REF_COLUMN)
                .map(f -> f.apply(dataModel, tableName)).filter(Optional::isPresent).map(Optional::get).findFirst();
    }

    private long getTotalNbOfRowsToDelete(String columnName, long upToMillis) throws SQLException {
        long totalNumberOfRows = 0;
        try (Connection connection = dataModel.getConnection(false)) {
            try (PreparedStatement countSt = countRowsSql(tableName, columnName, upToMillis).prepare(connection)) {
                try (ResultSet rs = countSt.executeQuery()) {
                    if (rs.next()) {
                        totalNumberOfRows = rs.getLong(1);
                        logger.info("Found " + totalNumberOfRows + " rows to be deleted from " + tableName);
                    }
                }
            }
        }
        return totalNumberOfRows;
    }

    private void deleteRowsInBatch(String columnName, long upToMillis, long totalNumberOfRows) {
        long remaining = totalNumberOfRows;
        while (remaining > 0) {
            long countToDelete = Math.min(remaining, BATCH_SIZE);
            try {
                executeBatch(columnName, upToMillis, countToDelete);
                remaining -= countToDelete;
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to execute batch for table " + tableName, e);
            }
        }
    }

    private void executeBatch(String columnName, long upToMillis, long countToDelete) throws SQLException {
        transactionService.runInIndependentTransaction(() -> {
            try (Connection connection = dataModel.getConnection(true);
                 PreparedStatement statement = deleteRowsSql(columnName, upToMillis, countToDelete).prepare(connection)) {
                logger.fine("Trying to execute one batch for cleaning table " + tableName);
                int deletedCount = statement.executeUpdate();
                if (deletedCount > 0) {
                    logger.info("Deleted " + deletedCount + " rows from table " + tableName + " containing entries with " + columnName +
                            " up to " + Instant.ofEpochMilli(upToMillis));
                }
            }
        });
    }

    private SqlBuilder deleteRowsSql(String columnName, long untilDate, long countToDelete) {
        SqlBuilder builder = new SqlBuilder("DELETE FROM ");
        builder.append(tableName);
        builder.append(" WHERE ");
        builder.append(columnName);
        builder.append(" <= ");
        builder.addLong(untilDate);
        builder.append(" AND rownum <= ");
        builder.addLong(countToDelete);
        return builder;
    }

    private SqlBuilder countRowsSql(String tableName, String columnName, long until) {
        SqlBuilder builder = new SqlBuilder("SELECT COUNT(*) FROM ");
        builder.append(tableName);
        builder.append(" WHERE ");
        builder.append(columnName);
        builder.append(" <= ");
        builder.addLong(until);
        return builder;
    }

    private boolean hasColumn(String columnName) throws SQLException {
        try (Connection connection = dataModel.getConnection(false)) {
            try (ResultSet resultSet = connection.getMetaData().getColumns(connection.getCatalog(), connection.getSchema(), tableName, columnName)) {
                return resultSet.next();
            }
        }
    }

}
