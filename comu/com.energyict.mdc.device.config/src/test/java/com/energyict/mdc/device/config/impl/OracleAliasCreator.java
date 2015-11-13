package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.orm.UnderlyingSQLFailedException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Create oracle specific table aliases that are required for
 * the relation components to work properly in integration tests.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-13 (16:19)
 */
public class OracleAliasCreator {
    static void createOracleAliases(Connection connection) {
        try {
            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "CREATE VIEW IF NOT EXISTS USER_TABLES AS select table_name from INFORMATION_SCHEMA.TABLES where table_schema = 'PUBLIC'"
            )) {
                preparedStatement.execute();
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "CREATE VIEW IF NOT EXISTS USER_IND_COLUMNS AS select index_name, table_name, column_name, ordinal_position AS column_position from INFORMATION_SCHEMA.INDEXES where table_schema = 'PUBLIC'"
            )) {
                preparedStatement.execute();
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS USER_SEQUENCES ( SEQUENCE_NAME VARCHAR2 (30) NOT NULL, MIN_VALUE NUMBER, MAX_VALUE NUMBER, INCREMENT_BY NUMBER NOT NULL, CYCLE_FLAG VARCHAR2 (1), ORDER_FLAG VARCHAR2 (1), CACHE_SIZE NUMBER NOT NULL, LAST_NUMBER NUMBER NOT NULL)"
            )) {
                preparedStatement.execute();
            }
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }
}