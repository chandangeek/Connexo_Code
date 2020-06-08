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
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

@ConsumerType
public interface FullInstaller {

    void install(DataModelUpgrader dataModelUpgrader, Logger logger);

    default void doTry(String description, Runnable runnable, Logger logger) {
        try {
            logger.log(Level.INFO, "Start   : " + description);
            runnable.run();
            logger.log(Level.INFO, "Success : " + description);
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Failed  : " + description, e);
            throw e;
        }
    }

    default <T> T doTry(String description, Callable<T> callable, Logger logger) {
        try {
            logger.log(Level.INFO, "Start   : " + description);
            T value = callable.call();
            logger.log(Level.INFO, "Success : " + description);
            return value;
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Failed  : " + description, e);
            throw e;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed  : " + description, e);
            throw new RuntimeException(e);
        }
    }

    default <T> T executeQuery(Statement statement, String sql, SqlExceptionThrowingFunction<ResultSet, T> resultMapper) {
        try (ResultSet resultSet = statement.executeQuery(sql)) {
            return resultMapper.apply(resultSet);
        } catch (SQLException e) {
            String name = this.getClass().getName();
            Logger.getLogger(name)
                    .severe("Exception during installation by " + name + System.lineSeparator() +
                            "Failed statement: " + sql);
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

    default void execute(Statement statement, String sql) {
        try {
            statement.execute(sql);
        } catch (SQLException e) {
            String name = this.getClass().getName();
            Logger.getLogger(name)
                    .severe("Exception during installation by " + name + System.lineSeparator() +
                            "Failed statement: " + sql);
            throw new UnderlyingSQLFailedException(e);
        }
    }

    default void execute(DataModel dataModel, String... sql) {
        dataModel.useConnectionRequiringTransaction(connection -> {
            try (Statement statement = connection.createStatement()) {
                Arrays.stream(sql).forEach(command -> execute(statement, command));
            }
        });
    }
}
