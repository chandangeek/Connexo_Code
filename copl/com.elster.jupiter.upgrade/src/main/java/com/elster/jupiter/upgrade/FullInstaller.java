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

    default String getRefreshJob(String jobName, String tableName, String createTableStatement, int minRefreshInterval) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(" BEGIN ");
        sqlBuilder.append(" DBMS_SCHEDULER.CREATE_JOB  ");
        sqlBuilder.append(" ( ");
        sqlBuilder.append(" JOB_NAME            => '").append(jobName).append("', ");
        sqlBuilder.append(" JOB_TYPE            => 'PLSQL_BLOCK', ");
        sqlBuilder.append(" JOB_ACTION          => ' ");
        sqlBuilder.append(" BEGIN ");
        sqlBuilder.append(" execute immediate ''DROP TABLE ").append(tableName).append("''; ");
        sqlBuilder.append(" execute immediate ");
        sqlBuilder.append(" ''");
        sqlBuilder.append(createTableStatement.replace("'", "''''"));
        sqlBuilder.append(" ''; ");
        sqlBuilder.append(" EXCEPTION ");
        sqlBuilder.append("    WHEN OTHERS THEN ");
        sqlBuilder.append("       IF SQLCODE != -942 THEN ");
        sqlBuilder.append("          RAISE; ");
        sqlBuilder.append("       END IF; ");
        sqlBuilder.append(" END;', ");
        sqlBuilder.append(" NUMBER_OF_ARGUMENTS => 0, ");
        sqlBuilder.append(" START_DATE          => SYSTIMESTAMP, ");
        sqlBuilder.append(" REPEAT_INTERVAL     => 'FREQ=MINUTELY;INTERVAL=").append(minRefreshInterval).append("', ");
        sqlBuilder.append(" END_DATE            => NULL, ");
        sqlBuilder.append(" ENABLED             => TRUE, ");
        sqlBuilder.append(" AUTO_DROP           => FALSE, ");
        sqlBuilder.append(" COMMENTS            => 'JOB TO REFRESH' ");
        sqlBuilder.append(" ); ");
        sqlBuilder.append(" END;");
        return sqlBuilder.toString();
    }
}
