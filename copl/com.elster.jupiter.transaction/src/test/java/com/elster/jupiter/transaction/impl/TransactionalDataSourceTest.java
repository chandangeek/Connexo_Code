/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.transaction.impl;

import oracle.jdbc.OracleConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TransactionalDataSourceTest {

    private static final int TIMEOUT = 51561;
    private TransactionalDataSource transactionalDataSource;

    @Mock
    private TransactionServiceImpl transactionService;
    @Mock
    private OracleConnection connection;
    @Mock
    private DataSource dataSource;

    @Before
    public void setUp() throws SQLException {
        transactionalDataSource = new TransactionalDataSource();
        transactionalDataSource.setTransactionService(transactionService);

        when(transactionService.getConnection()).thenReturn(connection);
        when(connection.unwrap(OracleConnection.class)).thenReturn(connection);
        when(transactionService.getDataSource()).thenReturn(dataSource);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testGetConnectionDelegatesToTransactionService() throws SQLException {
        assertThat(transactionalDataSource.getConnection().unwrap(OracleConnection.class)).isSameAs(connection);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetConnectionWithUserNamePasswordIsNotSupported() throws SQLException {
        transactionalDataSource.getConnection("userName", "password");
    }

    @Test
    public void testGetLogWriterSimplyDelegates() throws SQLException {
        PrintWriter mock = new PrintWriter(mock(Writer.class));
        when(dataSource.getLogWriter()).thenReturn(mock);

        assertThat(transactionalDataSource.getLogWriter()).isEqualTo(mock);
    }

    @Test
    public void testGetLoginTimeout() throws SQLException {
        when(dataSource.getLoginTimeout()).thenReturn(TIMEOUT);

        assertThat(transactionalDataSource.getLoginTimeout()).isEqualTo(TIMEOUT);
    }

    @Test
    public void testGetParentLogger() throws SQLFeatureNotSupportedException {
        Logger logger = mock(Logger.class);
        when(dataSource.getParentLogger()).thenReturn(logger);

        assertThat(transactionalDataSource.getParentLogger()).isEqualTo(logger);
    }

    @Test
    public void testIsWrapperForTransactionalDataSource() throws SQLException {
        assertThat(transactionalDataSource.isWrapperFor(TransactionalDataSource.class)).isTrue();
    }

    @Test
    public void testIsWrapperForSomethingUnderlyingDataSourceWraps() throws SQLException {
        when(dataSource.isWrapperFor(Comparable.class)).thenReturn(true);

        assertThat(transactionalDataSource.isWrapperFor(Comparable.class)).isTrue();
    }

    @Test
    public void testIsWrapperFalse() throws SQLException {
        when(dataSource.isWrapperFor(Comparable.class)).thenReturn(false);

        assertThat(transactionalDataSource.isWrapperFor(Comparable.class)).isFalse();
    }

    @Test
    public void testSetLoginTimeout() throws SQLException {
        transactionalDataSource.setLoginTimeout(TIMEOUT);

        verify(dataSource).setLoginTimeout(TIMEOUT);
    }

    @Test
    public void testSetLogWriter() throws SQLException {
        PrintWriter printWriter = new PrintWriter(new StringWriter());
        transactionalDataSource.setLogWriter(printWriter);

        verify(dataSource).setLogWriter(printWriter);
    }

    @Test
    public void testUnwrapForTransactionalDataSource() throws SQLException {
        assertThat(transactionalDataSource.unwrap(TransactionalDataSource.class)).isEqualTo(transactionalDataSource);
    }

    @Test
    public void testUnwrapForSomethingUnderlyingDataSourceWraps() throws SQLException {
        Comparable<?> comparable = "";
        when(dataSource.unwrap(Comparable.class)).thenReturn(comparable);

        assertThat(transactionalDataSource.unwrap(Comparable.class)).isEqualTo(comparable);
    }

    @SuppressWarnings("unchecked")
	@Test(expected = SQLException.class)
    public void testUnwrapFalse() throws SQLException {
        when(dataSource.unwrap(Comparable.class)).thenThrow(SQLException.class);
        transactionalDataSource.unwrap(Comparable.class);
    }


}
