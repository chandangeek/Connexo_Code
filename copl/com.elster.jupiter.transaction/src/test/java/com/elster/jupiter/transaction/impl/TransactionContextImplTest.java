package com.elster.jupiter.transaction.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.SQLException;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class TransactionContextImplTest {

    private TransactionContextImpl transactionContext;

    @Mock
    private TransactionServiceImpl transactionService;
    @Mock
    private Connection connection;

    @Before
    public void setUp() throws SQLException {
        doReturn(connection).when(transactionService).newConnection(false);

        transactionContext = new TransactionContextImpl(transactionService);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testTerminateWithCommitClosesUnderlyingConnection() throws SQLException {

        transactionContext.getConnection();
        transactionContext.terminate(true);

        verify(connection).close();

    }

    @Test
    public void testTerminateWithRollbackClosesUnderlyingConnection() throws SQLException {

        transactionContext.getConnection();
        transactionContext.terminate(false);

        verify(connection).close();

    }

    @Test
    public void testTerminateWithCommitCommitsUnderlyingConnection() throws SQLException {

        transactionContext.getConnection();
        transactionContext.terminate(true);

        verify(connection).commit();

    }

    @Test
    public void testTerminateWithRollbackRollsBackUnderlyingConnection() throws SQLException {

        transactionContext.getConnection();
        transactionContext.terminate(false);

        verify(connection).rollback();

    }

    @Test
    public void testTerminateDoesNothingWhenConnectionWasNotUsed() throws SQLException {
        transactionContext.terminate(true);

        verify(connection, never()).commit();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCannotCommitOnConnectionDirectly() throws SQLException {
        transactionContext.getConnection().commit();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCannotRollbackOnConnectionDirectly() throws SQLException {
        transactionContext.getConnection().rollback();
    }

    @Test
    public void testCloseIsIgnoredOnConnectionDirectly() throws SQLException {
        transactionContext.getConnection().close();

        verify(connection, never()).close();
    }

    @Test
    public void testSetRollbackOnly() throws SQLException {

        transactionContext.getConnection();
        transactionContext.setRollbackOnly();
        transactionContext.terminate(true);

        verify(connection, never()).commit();
        verify(connection).rollback();

    }


}
