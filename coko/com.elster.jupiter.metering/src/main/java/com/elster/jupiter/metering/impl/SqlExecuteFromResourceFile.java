/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Executes a sql statement that is contained in one of this bundle's resource files.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-22 (15:37)
 */
class SqlExecuteFromResourceFile {

    private final Connection connection;
    private final String resourceName;

    public static void executeAll(DataModel dataModel, String... resources) {
        try (Connection connection = dataModel.getConnection(true)) {
            Stream.of(resources).forEach(resource -> SqlExecuteFromResourceFile.execute(resource, connection));
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    public static void execute(String resourceName, Connection connection) {
        new SqlExecuteFromResourceFile(connection, resourceName).execute();
    }

    private SqlExecuteFromResourceFile(Connection connection, String resourceName) {
        this.connection = connection;
        this.resourceName = resourceName;
    }

    private void execute() {
        try (InputStream sqlInputStream = this.getClass().getResourceAsStream(resourceName)) {
            if (sqlInputStream != null) {
                this.execute(sqlInputStream);
            } else {
                throw new IllegalArgumentException("Sql resource file not found: " + resourceName);
            }
        } catch (IOException e) {
            throw new UnderlyingIOException(e);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private void execute(InputStream sqlInputStream) throws SQLException {
        this.execute(new BufferedReader(new InputStreamReader(sqlInputStream)));
    }

    private void execute(BufferedReader sqlReader) throws SQLException {
        SqlBuilder sqlBuilder = new SqlBuilder();
        try {
            sqlBuilder.append(
                sqlReader
                    .lines()
                    .filter(this::notEmpty)
                    .collect(Collectors.joining("\n")));
        } catch (UncheckedIOException e) {
            throw new UnderlyingIOException(e.getCause());
        }
        try (PreparedStatement statement = sqlBuilder.prepare(this.connection)) {
            statement.executeUpdate();
        }
    }

    private boolean notEmpty(String s) {
        return !s.isEmpty();
    }

}