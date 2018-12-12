/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link SqlExecuteFromResourceFile} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-22 (16:13)
 */
@RunWith(MockitoJUnitRunner.class)
public class SqlExecuteFromResourceFileTest {

    @Mock
    private DataModel dataModel;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement statement;

    @Before
    public void initializeMocks() throws SQLException {
        when(this.connection.prepareStatement(anyString())).thenReturn(this.statement);
        when(this.dataModel.getConnection(true)).thenReturn(this.connection);
    }

    @Test
    public void testSingleStatement() throws SQLException {
        // Business method
        SqlExecuteFromResourceFile.execute("singleStatement.sql", this.connection);

        // Asserts
        ArgumentCaptor<String> statementCaptor = ArgumentCaptor.forClass(String.class);
        verify(this.connection).prepareStatement(statementCaptor.capture());
        String sqlStatement = statementCaptor.getValue();
        assertThat(sqlStatement).isEqualTo("Statement line #1");
        verify(this.statement).executeUpdate();
        verify(this.statement).close();
        verify(this.connection, never()).close();
    }

    @Test
    public void testMultipleStatements() throws SQLException {
        // Business method
        SqlExecuteFromResourceFile.execute("multipleStatements.sql", this.connection);

        // Asserts
        ArgumentCaptor<String> statementCaptor = ArgumentCaptor.forClass(String.class);
        verify(this.connection).prepareStatement(statementCaptor.capture());
        String sqlStatement = statementCaptor.getValue();
        assertThat(sqlStatement).isEqualTo("Statement line #1\nStatement line #2\nLAST statement");
        verify(this.statement).executeUpdate();
        verify(this.statement).close();
        verify(this.connection, never()).close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingResource() throws SQLException {
        // Business method
        SqlExecuteFromResourceFile.execute("missing.sql", this.connection);

        // Asserts: see expected exception rule
    }

    @Test(expected = UnderlyingSQLFailedException.class)
    public void testSqlException() throws SQLException {
        doThrow(SQLException.class).when(this.connection).prepareStatement(anyString());

        // Business method
        SqlExecuteFromResourceFile.execute("singleStatement.sql", this.connection);

        // Asserts: see expected exception rule
    }

    @Test
    public void executeAllClosesTheConnection() throws SQLException {
        // Business method
        SqlExecuteFromResourceFile.executeAll(this.dataModel, "singleStatement.sql");

        // Asserts
        verify(this.connection).close();
    }

    @Test
    public void executeAllReallyExecutesAll() throws SQLException {
        // Business method
        SqlExecuteFromResourceFile.executeAll(this.dataModel, "singleStatement.sql", "multipleStatements.sql");

        // Asserts
        verify(this.connection, times(2)).prepareStatement(anyString());
    }

    @Test(expected = UnderlyingSQLFailedException.class)
    public void executeAllWrapsSqlException() throws SQLException {
        doThrow(SQLException.class).when(this.dataModel).getConnection(true);

        // Business method
        SqlExecuteFromResourceFile.executeAll(this.dataModel, "singleStatement.sql");

        // Asserts: see expected exception rule
    }

}