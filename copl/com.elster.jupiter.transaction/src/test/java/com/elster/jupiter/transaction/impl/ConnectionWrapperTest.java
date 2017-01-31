/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.transaction.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import static org.mockito.Mockito.*;

@SuppressWarnings("resource")
@RunWith(MockitoJUnitRunner.class)
public class ConnectionWrapperTest {

    @Mock
    private Connection connection;
    @Mock
    private Executor executor;
    @Mock
    private Map<String, Class<?>> map;
    @Mock
    private Savepoint savepoint;

    @Test
    public void testDelegationOfabort() throws SQLException {
        new ConnectionInTransaction(connection).abort(executor);
        verify(connection).abort(executor);
    }

    @Test
    public void testDelegationOfcreateStatementWithoutArgs() throws SQLException {
        new ConnectionInTransaction(connection).createStatement();
        verify(connection).createStatement();
    }

    @Test
    public void testDelegationOfprepareStatementWithString() throws SQLException {
        new ConnectionInTransaction(connection).prepareStatement("sql");
        verify(connection).prepareStatement("sql");
    }

    @Test
    public void testDelegationOfprepareCallWithString() throws SQLException {
        new ConnectionInTransaction(connection).prepareCall("sql");
        verify(connection).prepareCall("sql");
    }

    @Test
    public void testDelegationOfnativeSQL() throws SQLException {
        new ConnectionInTransaction(connection).nativeSQL("sql");
        verify(connection).nativeSQL("sql");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDelegationOfsetAutoCommit() throws SQLException {
        new ConnectionInTransaction(connection).setAutoCommit(true);
    }

    @Test
    public void testDelegationOfgetAutoCommit() throws SQLException {
        new ConnectionInTransaction(connection).getAutoCommit();
        verify(connection).getAutoCommit();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDelegationOfcommit() throws SQLException {
        new ConnectionInTransaction(connection).commit();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDelegationOfrollbackNoArgs() throws SQLException {
        new ConnectionInTransaction(connection).rollback();
    }

    @Test
    public void testDelegationOfclose() throws SQLException {
        new ConnectionInTransaction(connection).close();
        verify(connection, never()).close();
    }

    @Test
    public void testDelegationOfisClosed() throws SQLException {
        new ConnectionInTransaction(connection).isClosed();
        verify(connection).isClosed();
    }

    @Test
    public void testDelegationOfgetMetaData() throws SQLException {
        new ConnectionInTransaction(connection).getMetaData();
        verify(connection).getMetaData();
    }

    @Test
    public void testDelegationOfsetReadOnly() throws SQLException {
        new ConnectionInTransaction(connection).setReadOnly(true);
        verify(connection).setReadOnly(true);
    }

    @Test
    public void testDelegationOfisReadOnly() throws SQLException {
        new ConnectionInTransaction(connection).isReadOnly();
        verify(connection).isReadOnly();
    }

    @Test
    public void testDelegationOfsetCatalog() throws SQLException {
        new ConnectionInTransaction(connection).setCatalog("catalog");
        verify(connection).setCatalog("catalog");
    }

    @Test
    public void testDelegationOfgetCatalog() throws SQLException {
        new ConnectionInTransaction(connection).getCatalog();
        verify(connection).getCatalog();
    }

    @Test
    public void testDelegationOfsetTransactionIsolation() throws SQLException {
        new ConnectionInTransaction(connection).setTransactionIsolation(1);
        verify(connection).setTransactionIsolation(1);
    }

    @Test
    public void testDelegationOfgetTransactionIsolation() throws SQLException {
        new ConnectionInTransaction(connection).getTransactionIsolation();
        verify(connection).getTransactionIsolation();
    }

    @Test
    public void testDelegationOfgetWarnings() throws SQLException {
        new ConnectionInTransaction(connection).getWarnings();
        verify(connection).getWarnings();
    }

    @Test
    public void testDelegationOfclearWarnings() throws SQLException {
        new ConnectionInTransaction(connection).clearWarnings();
        verify(connection).clearWarnings();
    }

    @Test
    public void testDelegationOfcreateStatementWithTypeAndConcurrency() throws SQLException {
        new ConnectionInTransaction(connection).createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        verify(connection).createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    }

    @Test
    public void testDelegationOfprepareStatementWithStringAndTypeAndConcurrency() throws SQLException {
        new ConnectionInTransaction(connection).prepareStatement("sql", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        verify(connection).prepareStatement("sql", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    }

    @Test
    public void testDelegationOfprepareCallWithStringAndTypeAndConcurrency() throws SQLException {
        new ConnectionInTransaction(connection).prepareCall("sql", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        verify(connection).prepareCall("sql", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    }

    @Test
    public void testDelegationOfGetTypeMap() throws SQLException {
        new ConnectionInTransaction(connection).getTypeMap();
        verify(connection).getTypeMap();
    }

    @Test
    public void testDelegationOfsetTypeMap() throws SQLException {
        new ConnectionInTransaction(connection).setTypeMap(map);
        verify(connection).setTypeMap(map);
    }

    @Test
    public void testDelegationOfsetHoldability() throws SQLException {
        new ConnectionInTransaction(connection).setHoldability(1);
        verify(connection).setHoldability(1);
    }

    @Test
    public void testDelegationOfgetHoldability() throws SQLException {
        new ConnectionInTransaction(connection).getHoldability();
        verify(connection).getHoldability();
    }

    @Test
    public void testDelegationOfsetSavepointNoArgs() throws SQLException {
        new ConnectionInTransaction(connection).setSavepoint();
        verify(connection).setSavepoint();
    }

    @Test
    public void testDelegationOfsetSavepointWithName() throws SQLException {
        new ConnectionInTransaction(connection).setSavepoint("name");
        verify(connection).setSavepoint("name");
    }

    @Test
    public void testDelegationOfrollback() throws SQLException {
        new ConnectionInTransaction(connection).rollback(savepoint);
        verify(connection).rollback(savepoint);
    }

    @Test
    public void testDelegationOfreleaseSavepoint() throws SQLException {
        new ConnectionInTransaction(connection).releaseSavepoint(savepoint);
        verify(connection).releaseSavepoint(savepoint);
    }

    @Test
    public void testDelegationOfcreateStatement() throws SQLException {
        new ConnectionInTransaction(connection).createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
        verify(connection).createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
    }

    @Test
    public void testDelegationOfprepareCall() throws SQLException {
        new ConnectionInTransaction(connection).prepareCall("sql", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, 1);
        verify(connection).prepareCall("sql", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, 1);
    }

    @Test
    public void testDelegationOfprepareStatementWithStringAndAutoGeneratedKeys() throws SQLException {
        new ConnectionInTransaction(connection).prepareStatement("sql", Statement.RETURN_GENERATED_KEYS);
        verify(connection).prepareStatement("sql", Statement.RETURN_GENERATED_KEYS);
    }

    @Test
    public void testDelegationOfprepareStatementWithStringAndColumnIndexes() throws SQLException {
        int[] columnIndexes = {};
        new ConnectionInTransaction(connection).prepareStatement("sql", columnIndexes);
        verify(connection).prepareStatement("sql", columnIndexes);
    }

    @Test
    public void testDelegationOfprepareStatement() throws SQLException {
        String[] columnNames = {};
        new ConnectionInTransaction(connection).prepareStatement("sql", columnNames);
        verify(connection).prepareStatement("sql", columnNames);
    }

    @Test
    public void testDelegationOfcreateClob() throws SQLException {
        new ConnectionInTransaction(connection).createClob();
        verify(connection).createClob();
    }

    @Test
    public void testDelegationOfcreateBlob() throws SQLException {
        new ConnectionInTransaction(connection).createBlob();
        verify(connection).createBlob();
    }

    @Test
    public void testDelegationOfcreateNClob() throws SQLException {
        new ConnectionInTransaction(connection).createNClob();
        verify(connection).createNClob();
    }

    @Test
    public void testDelegationOfcreateSQLXML() throws SQLException {
        new ConnectionInTransaction(connection).createSQLXML();
        verify(connection).createSQLXML();
    }

    @Test
    public void testDelegationOfisValid() throws SQLException {
        new ConnectionInTransaction(connection).isValid(1000);
        verify(connection).isValid(1000);
    }

    public void setClientInfo() throws SQLClientInfoException {
        new ConnectionInTransaction(connection).setClientInfo("name", "value");
        verify(connection).setClientInfo("name", "value");
    }

    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        new ConnectionInTransaction(connection).setClientInfo("name", "value");
        verify(connection).setClientInfo("name", "value");
    }

    @Test
    public void testDelegationOfgetClientInfoByName() throws SQLException {
        new ConnectionInTransaction(connection).getClientInfo("name");
        verify(connection).getClientInfo("name");
    }

    @Test
    public void testDelegationOfgetClientInfo() throws SQLException {
        new ConnectionInTransaction(connection).getClientInfo();
        verify(connection).getClientInfo();
    }

    @Test
    public void testDelegationOfcreateArrayOf() throws SQLException {
        Object[] elements = {};
        new ConnectionInTransaction(connection).createArrayOf("VARCHAR", elements);
        verify(connection).createArrayOf("VARCHAR", elements);
    }

    @Test
    public void testDelegationOfcreateStruct() throws SQLException {
        Object[] attributes = {};
        new ConnectionInTransaction(connection).createStruct("VARCHAR", attributes);
        verify(connection).createStruct("VARCHAR", attributes);
    }

    @Test
    public void testDelegationOfsetSchema() throws SQLException {
        new ConnectionInTransaction(connection).setSchema("scheme");
        verify(connection).setSchema("scheme");
    }

    @Test
    public void testDelegationOfgetSchema() throws SQLException {
        new ConnectionInTransaction(connection).getSchema();
        verify(connection).getSchema();
    }

    @Test
    public void testDelegationOfsetNetworkTimeout() throws SQLException {
        new ConnectionInTransaction(connection).setNetworkTimeout(executor, 2500);
        verify(connection).setNetworkTimeout(executor, 2500);
    }

	@Test
    public void testDelegationOfgetNetworkTimeout() throws SQLException {
        new ConnectionInTransaction(connection).getNetworkTimeout();
        verify(connection).getNetworkTimeout();
    }

    @Test
    public void testDelegationOfUnwrap() throws SQLException {
        new ConnectionInTransaction(connection).unwrap(MySpecialConnection.class);
        verify(connection).unwrap(MySpecialConnection.class);
    }

    @Test
    public void testUnwrapDirectlyWrapping() throws SQLException {
        MySpecialConnection mySpecialConnection = mock(MySpecialConnection.class);
        new ConnectionInTransaction(mySpecialConnection).unwrap(MySpecialConnection.class);
        verify(mySpecialConnection).unwrap(MySpecialConnection.class);

    }

    @Test
    public void testDelegationOfisWrapperFor() throws SQLException {
        new ConnectionInTransaction(connection).isWrapperFor(MySpecialConnection.class);
        verify(connection).isWrapperFor(MySpecialConnection.class);
    }

    @Test
    public void testIsWrapperForDirectlyWrapping() throws SQLException {
        MySpecialConnection mySpecialConnection = mock(MySpecialConnection.class);
        new ConnectionInTransaction(mySpecialConnection).isWrapperFor(MySpecialConnection.class);
        verify(mySpecialConnection).isWrapperFor(MySpecialConnection.class);
    }

    private interface MySpecialConnection extends Connection {

    }
}
