/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.transaction.impl;

import com.elster.jupiter.util.Registration;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class TransactionContextImplTest {

    private TransactionState transactionContext;

    @Mock
    private TransactionServiceImpl transactionService;
    @Mock
    private Connection connection;
    @Mock
    private Registration registration;

    @Before
    public void setUp() throws SQLException {
        doReturn(connection).when(transactionService).newConnection(false);
        doReturn(registration).when(transactionService).addThreadSubscriber(any());
        transactionContext = new TransactionState(transactionService);
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
