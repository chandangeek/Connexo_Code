/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.upgrade;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;

import aQute.bnd.annotation.ConsumerType;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.logging.Logger;

@ConsumerType
public interface Upgrader {

    void migrate(DataModelUpgrader dataModelUpgrader);

    default void execute(DataModel dataModel, String... sql) {
        dataModel.useConnectionRequiringTransaction(connection -> {
            try (Statement statement = connection.createStatement()) {
                Arrays.stream(sql).forEach(command -> execute(statement, command));
            }
        });
    }

    default void execute(Statement statement, String sql) {
        try {
            statement.execute(sql);
        } catch (SQLException e) {
            String name = this.getClass().getName();
            String message = "Exception during upgrade by " + name + System.lineSeparator() +
                    "Failed statement: " + sql;
            Logger.getLogger(name).severe(message);
            System.err.println(message);
            throw new UnderlyingSQLFailedException(e);
        }
    }

    default <T> T executeQuery(DataModel dataModel, String sql, SqlExceptionThrowingFunction<ResultSet, T> resultMapper) {
        try (Connection connection = dataModel.getConnection(false);
             Statement statement = connection.createStatement()) {
            return executeQuery(statement, sql, resultMapper);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    default <T> T executeQuery(Statement statement, String sql, SqlExceptionThrowingFunction<ResultSet, T> resultMapper) {
        try (ResultSet resultSet = statement.executeQuery(sql)) {
            return resultMapper.apply(resultSet);
        } catch (SQLException e) {
            String name = this.getClass().getName();
            String message = "Exception during upgrade by " + name + System.lineSeparator() +
                    "Failed statement: " + sql;
            Logger.getLogger(name).severe(message);
            System.err.println(message);
            throw new UnderlyingSQLFailedException(e);
        }
    }
}
