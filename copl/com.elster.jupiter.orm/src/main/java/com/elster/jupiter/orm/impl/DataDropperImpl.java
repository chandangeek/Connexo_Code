/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataDropper;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class DataDropperImpl implements DataDropper {
    private static final long BATCH_SIZE = 50_000L;
    private static final long DELAY_BETWEEN_BATCH_DELETE = 1000L;
    private final DataModelImpl dataModel;
    private final String tableName;
    private final Logger logger;
    private Instant upTo;

    private static final BiFunction<DataModelImpl, String, Optional<String>> TABLE_REF_COLUMN = (dataMdl, tblName) ->
            Optional.<TableImpl>ofNullable(dataMdl.getTable(tblName)).<Column>flatMap(TableImpl::partitionColumn).<String>map(Column::getName);


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
        if (Stream.of("JRNL", "JNRL").filter(suffix -> tblName.toUpperCase().endsWith(suffix)).findFirst().isPresent()) {
            return Optional.of(referenceColumn);
        }
        return Optional.empty();
    };


    DataDropperImpl(DataModelImpl dataModel, String tableName, Logger logger) {
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
            long totalNbOfRowsToDelete = getTotalNbOfRowsToDelete(columnName.get(), upToMillis);
            deleteRowsInBatch(columnName.get(), upToMillis, totalNbOfRowsToDelete);
        } else {
            logger.warning("Cannot delete rows from table " + tableName + "! No reference column found!");
        }
    }

    private long getTotalNbOfRowsToDelete(String columnName, long upToMillis) throws SQLException {
        long totalNbOfRowsToDelete = 0;
        try (Connection connection = dataModel.getConnection(false)) {
            try (PreparedStatement countSt = countRowsSql(tableName, columnName, upToMillis).prepare(connection)) {
                try (ResultSet rs = countSt.executeQuery()) {
                    if (rs.next()) {
                        totalNbOfRowsToDelete = rs.getLong(1);
                        logger.info("Found " + totalNbOfRowsToDelete + " rows to be deleted from " + tableName);
                    }
                }
            }
        }
        return totalNbOfRowsToDelete;
    }

    private void deleteRowsInBatch(String columnName, long upToMillis, long totalNbOfRowsToDelete) throws SQLException {
        while (totalNbOfRowsToDelete > 0) {
            long nbOfRowsToBeDeleted = Math.min(totalNbOfRowsToDelete, BATCH_SIZE);
            try (Connection connection = dataModel.getConnection(true)) {
                try (PreparedStatement deleteSt = prepareDeleteRowsSql(tableName, columnName, upToMillis, nbOfRowsToBeDeleted, connection)) {
                    int nbOfDeletedRows = deleteSt.executeUpdate();
                    logger.info("Deleted " + nbOfRowsToBeDeleted + " rows from table " + tableName + " containing entries with " + columnName +
                            " up to " + Instant.ofEpochMilli(upToMillis));
                    if (nbOfDeletedRows == 0) {
                        return;
                    }
                }
            }
            totalNbOfRowsToDelete -= nbOfRowsToBeDeleted;
            delay(DELAY_BETWEEN_BATCH_DELETE);
        }
    }

    private Optional<String> getReferenceColumnName() {
        return Stream.of(TABLE_REF_COLUMN, IDS_TABLE_REF_COLUMN, JOURNAL_TABLE_REF_COLUMN)
                .map(f -> f.apply(dataModel, tableName)).filter(Optional::isPresent).map(Optional::get).findFirst();
    }

    private boolean hasColumn(String columnName) throws SQLException {
        try (Connection connection = dataModel.getConnection(false)) {
            try (ResultSet resultSet = connection.getMetaData().getColumns(connection.getCatalog(), connection.getSchema(), tableName, columnName)) {
                return resultSet.next();
            }
        }
    }

    private PreparedStatement prepareDeleteRowsSql(String tableName, String columnName, long untilDate, long untilRowNum, Connection connection) throws SQLException {
        SqlBuilder builder = deleteRowsSql(tableName, columnName, untilDate, untilRowNum);
        logger.fine("Command for rows deletion '" + builder.toString() + "'.");
        return builder.prepare(connection);
    }

    private SqlBuilder deleteRowsSql(String tableName, String columnName, long untilDate, long untilRowNum) {
        SqlBuilder builder = new SqlBuilder("DELETE FROM ");
        builder.append(tableName);
        builder.append(" WHERE ");
        builder.append(columnName);
        builder.append(" <= ");
        builder.addLong(untilDate);
        builder.append(" AND rownum <= ");
        builder.addLong(untilRowNum);
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

    private void delay(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
