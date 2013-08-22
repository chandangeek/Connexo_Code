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
        new ConnectionWrapper(connection).abort(executor);
        verify(connection).abort(executor);
    }

    @Test
    public void testDelegationOfcreateStatementWithoutArgs() throws SQLException {
        new ConnectionWrapper(connection).createStatement();
        verify(connection).createStatement();
    }

    @Test
    public void testDelegationOfprepareStatementWithString() throws SQLException {
        new ConnectionWrapper(connection).prepareStatement("sql");
        verify(connection).prepareStatement("sql");
    }

    @Test
    public void testDelegationOfprepareCallWithString() throws SQLException {
        new ConnectionWrapper(connection).prepareCall("sql");
        verify(connection).prepareCall("sql");
    }

    @Test
    public void testDelegationOfnativeSQL() throws SQLException {
        new ConnectionWrapper(connection).nativeSQL("sql");
        verify(connection).nativeSQL("sql");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDelegationOfsetAutoCommit() throws SQLException {
        new ConnectionWrapper(connection).setAutoCommit(true);
    }

    @Test
    public void testDelegationOfgetAutoCommit() throws SQLException {
        new ConnectionWrapper(connection).getAutoCommit();
        verify(connection).getAutoCommit();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDelegationOfcommit() throws SQLException {
        new ConnectionWrapper(connection).commit();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDelegationOfrollbackNoArgs() throws SQLException {
        new ConnectionWrapper(connection).rollback();
    }

    @Test
    public void testDelegationOfclose() throws SQLException {
        new ConnectionWrapper(connection).close();
        verify(connection, never()).close();
    }

    @Test
    public void testDelegationOfisClosed() throws SQLException {
        new ConnectionWrapper(connection).isClosed();
        verify(connection).isClosed();
    }

    @Test
    public void testDelegationOfgetMetaData() throws SQLException {
        new ConnectionWrapper(connection).getMetaData();
        verify(connection).getMetaData();
    }

    @Test
    public void testDelegationOfsetReadOnly() throws SQLException {
        new ConnectionWrapper(connection).setReadOnly(true);
        verify(connection).setReadOnly(true);
    }

    @Test
    public void testDelegationOfisReadOnly() throws SQLException {
        new ConnectionWrapper(connection).isReadOnly();
        verify(connection).isReadOnly();
    }

    @Test
    public void testDelegationOfsetCatalog() throws SQLException {
        new ConnectionWrapper(connection).setCatalog("catalog");
        verify(connection).setCatalog("catalog");
    }

    @Test
    public void testDelegationOfgetCatalog() throws SQLException {
        new ConnectionWrapper(connection).getCatalog();
        verify(connection).getCatalog();
    }

    @Test
    public void testDelegationOfsetTransactionIsolation() throws SQLException {
        new ConnectionWrapper(connection).setTransactionIsolation(1);
        verify(connection).setTransactionIsolation(1);
    }

    @Test
    public void testDelegationOfgetTransactionIsolation() throws SQLException {
        new ConnectionWrapper(connection).getTransactionIsolation();
        verify(connection).getTransactionIsolation();
    }

    @Test
    public void testDelegationOfgetWarnings() throws SQLException {
        new ConnectionWrapper(connection).getWarnings();
        verify(connection).getWarnings();
    }

    @Test
    public void testDelegationOfclearWarnings() throws SQLException {
        new ConnectionWrapper(connection).clearWarnings();
        verify(connection).clearWarnings();
    }

    @Test
    public void testDelegationOfcreateStatementWithTypeAndConcurrency() throws SQLException {
        new ConnectionWrapper(connection).createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        verify(connection).createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    }

    @Test
    public void testDelegationOfprepareStatementWithStringAndTypeAndConcurrency() throws SQLException {
        new ConnectionWrapper(connection).prepareStatement("sql", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        verify(connection).prepareStatement("sql", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    }

    @Test
    public void testDelegationOfprepareCallWithStringAndTypeAndConcurrency() throws SQLException {
        new ConnectionWrapper(connection).prepareCall("sql", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        verify(connection).prepareCall("sql", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    }

    @Test
    public void testDelegationOfGetTypeMap() throws SQLException {
        new ConnectionWrapper(connection).getTypeMap();
        verify(connection).getTypeMap();
    }

    @Test
    public void testDelegationOfsetTypeMap() throws SQLException {
        new ConnectionWrapper(connection).setTypeMap(map);
        verify(connection).setTypeMap(map);
    }

    @Test
    public void testDelegationOfsetHoldability() throws SQLException {
        new ConnectionWrapper(connection).setHoldability(1);
        verify(connection).setHoldability(1);
    }

    @Test
    public void testDelegationOfgetHoldability() throws SQLException {
        new ConnectionWrapper(connection).getHoldability();
        verify(connection).getHoldability();
    }

    @Test
    public void testDelegationOfsetSavepointNoArgs() throws SQLException {
        new ConnectionWrapper(connection).setSavepoint();
        verify(connection).setSavepoint();
    }

    @Test
    public void testDelegationOfsetSavepointWithName() throws SQLException {
        new ConnectionWrapper(connection).setSavepoint("name");
        verify(connection).setSavepoint("name");
    }

    @Test
    public void testDelegationOfrollback() throws SQLException {
        new ConnectionWrapper(connection).rollback(savepoint);
        verify(connection).rollback(savepoint);
    }

    @Test
    public void testDelegationOfreleaseSavepoint() throws SQLException {
        new ConnectionWrapper(connection).releaseSavepoint(savepoint);
        verify(connection).releaseSavepoint(savepoint);
    }

    @Test
    public void testDelegationOfcreateStatement() throws SQLException {
        new ConnectionWrapper(connection).createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, 1);
        verify(connection).createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, 1);
    }

    @Test
    public void testDelegationOfprepareCall() throws SQLException {
        new ConnectionWrapper(connection).prepareCall("sql", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, 1);
        verify(connection).prepareCall("sql", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, 1);
    }

    @Test
    public void testDelegationOfprepareStatementWithStringAndAutoGeneratedKeys() throws SQLException {
        new ConnectionWrapper(connection).prepareStatement("sql", Statement.RETURN_GENERATED_KEYS);
        verify(connection).prepareStatement("sql", Statement.RETURN_GENERATED_KEYS);
    }

    @Test
    public void testDelegationOfprepareStatementWithStringAndColumnIndexes() throws SQLException {
        int[] columnIndexes = {};
        new ConnectionWrapper(connection).prepareStatement("sql", columnIndexes);
        verify(connection).prepareStatement("sql", columnIndexes);
    }

    @Test
    public void testDelegationOfprepareStatement() throws SQLException {
        String[] columnNames = {};
        new ConnectionWrapper(connection).prepareStatement("sql", columnNames);
        verify(connection).prepareStatement("sql", columnNames);
    }

    @Test
    public void testDelegationOfcreateClob() throws SQLException {
        new ConnectionWrapper(connection).createClob();
        verify(connection).createClob();
    }

    @Test
    public void testDelegationOfcreateBlob() throws SQLException {
        new ConnectionWrapper(connection).createBlob();
        verify(connection).createBlob();
    }

    @Test
    public void testDelegationOfcreateNClob() throws SQLException {
        new ConnectionWrapper(connection).createNClob();
        verify(connection).createNClob();
    }

    @Test
    public void testDelegationOfcreateSQLXML() throws SQLException {
        new ConnectionWrapper(connection).createSQLXML();
        verify(connection).createSQLXML();
    }

    @Test
    public void testDelegationOfisValid() throws SQLException {
        new ConnectionWrapper(connection).isValid(1000);
        verify(connection).isValid(1000);
    }

    public void setClientInfo() throws SQLClientInfoException {
        new ConnectionWrapper(connection).setClientInfo("name", "value");
        verify(connection).setClientInfo("name", "value");
    }

    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        Properties props = new Properties();
        new ConnectionWrapper(connection).setClientInfo("name", "value");
        verify(connection).setClientInfo("name", "value");
    }

    @Test
    public void testDelegationOfgetClientInfoByName() throws SQLException {
        new ConnectionWrapper(connection).getClientInfo("name");
        verify(connection).getClientInfo("name");
    }

    @Test
    public void testDelegationOfgetClientInfo() throws SQLException {
        new ConnectionWrapper(connection).getClientInfo();
        verify(connection).getClientInfo();
    }

    @Test
    public void testDelegationOfcreateArrayOf() throws SQLException {
        Object[] elements = {};
        new ConnectionWrapper(connection).createArrayOf("VARCHAR", elements);
        verify(connection).createArrayOf("VARCHAR", elements);
    }

    @Test
    public void testDelegationOfcreateStruct() throws SQLException {
        Object[] attributes = {};
        new ConnectionWrapper(connection).createStruct("VARCHAR", attributes);
        verify(connection).createStruct("VARCHAR", attributes);
    }

    @Test
    public void testDelegationOfsetSchema() throws SQLException {
        new ConnectionWrapper(connection).setSchema("scheme");
        verify(connection).setSchema("scheme");
    }

    @Test
    public void testDelegationOfgetSchema() throws SQLException {
        new ConnectionWrapper(connection).getSchema();
        verify(connection).getSchema();
    }

    @Test
    public void testDelegationOfsetNetworkTimeout() throws SQLException {
        new ConnectionWrapper(connection).setNetworkTimeout(executor, 2500);
        verify(connection).setNetworkTimeout(executor, 2500);
    }

    @Test
    public void testDelegationOfgetNetworkTimeout() throws SQLException {
        new ConnectionWrapper(connection).getNetworkTimeout();
        verify(connection).getNetworkTimeout();
    }

    @Test
    public void testDelegationOfUnwrap() throws SQLException {
        new ConnectionWrapper(connection).unwrap(MySpecialConnection.class);
        verify(connection).unwrap(MySpecialConnection.class);
    }

    @Test
    public void testUnwrapDirectlyWrapping() throws SQLException {
        MySpecialConnection mySpecialConnection = mock(MySpecialConnection.class);
        new ConnectionWrapper(mySpecialConnection).unwrap(MySpecialConnection.class);
        verify(mySpecialConnection).unwrap(MySpecialConnection.class);

    }

    @Test
    public void testDelegationOfisWrapperFor() throws SQLException {
        new ConnectionWrapper(connection).isWrapperFor(MySpecialConnection.class);
        verify(connection).isWrapperFor(MySpecialConnection.class);
    }

    @Test
    public void testIsWrapperForDirectlyWrapping() throws SQLException {
        MySpecialConnection mySpecialConnection = mock(MySpecialConnection.class);
        new ConnectionWrapper(mySpecialConnection).isWrapperFor(MySpecialConnection.class);
        verify(mySpecialConnection).isWrapperFor(MySpecialConnection.class);
    }

    private interface MySpecialConnection extends Connection {

    }
}
