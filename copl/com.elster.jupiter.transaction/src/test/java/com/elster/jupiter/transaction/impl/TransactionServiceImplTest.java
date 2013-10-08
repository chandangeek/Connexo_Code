package com.elster.jupiter.transaction.impl;

import com.elster.jupiter.bootstrap.BootstrapService;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.CommitException;
import com.elster.jupiter.transaction.NestedTransactionException;
import com.elster.jupiter.transaction.NotInTransactionException;
import com.elster.jupiter.transaction.VoidTransaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TransactionServiceImplTest {

    private TransactionServiceImpl transactionService;

    private TransactionalDataSource transactionalDataSource;

    @Mock
    private BootstrapService bootStrapService;
    @Mock
    private DataSource dataSource;
    @Mock
    private ThreadPrincipalService threadPrincipalService;
    @Mock
    private Connection connection;
    @Mock
    private SQLException sqlException;
    @Mock
    private Publisher publisher;

    @Before
    public void setUp() throws SQLException {
        when(bootStrapService.createDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);

        transactionService = new TransactionServiceImpl();

        transactionService.setBootstrapService(bootStrapService);
        transactionService.setThreadPrincipalService(threadPrincipalService);
        transactionService.setPublisher(publisher);
        transactionalDataSource = new TransactionalDataSource();
        transactionalDataSource.setTransactionService(transactionService);

        Bus.setServiceLocator(transactionService);
    }

    @After
    public void tearDown() {
        Bus.setServiceLocator(null);
    }

    @Test(expected = NestedTransactionException.class)
    public void testThrowNestedTransactionExceptionIfAlreadyInTransaction() {
        final VoidTransaction inner = new VoidTransaction() {
            @Override
            protected void doPerform() {
                // do nothing
            }
        };
        VoidTransaction outer = new VoidTransaction() {
            @Override
            protected void doPerform() {
                transactionService.execute(inner);
            }
        };
        transactionService.execute(outer);
    }

    @Test
    public void testSimpleTransaction() throws IOException, SQLException {
        final Writer writer = spy(new StringWriter());
        VoidTransaction simple = new VoidTransaction() {
            @Override
            protected void doPerform() {
                try {
                    transactionalDataSource.getConnection();
                    writer.write("ok");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        transactionService.execute(simple);

        InOrder inOrder = inOrder(writer, connection);
        inOrder.verify(connection).setAutoCommit(false);
        inOrder.verify(writer).write("ok");
        inOrder.verify(connection).commit();
    }

    @Test
    public void testSimpleTransactionCallsRollbackOnly() throws IOException, SQLException {
        final Writer writer = spy(new StringWriter());
        VoidTransaction simple = new VoidTransaction() {
            @Override
            protected void doPerform() {
                try {
                    transactionalDataSource.getConnection();
                    transactionService.setRollbackOnly();
                    writer.write("ok");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        transactionService.execute(simple);

        InOrder inOrder = inOrder(writer, connection);
        inOrder.verify(connection).setAutoCommit(false);
        inOrder.verify(writer).write("ok");
        inOrder.verify(connection).rollback();
    }

    @Test
    public void testCommitFailureIsWrappedInCommitException() throws IOException, SQLException {
        doThrow(sqlException).when(connection).commit();
        VoidTransaction simple = new VoidTransaction() {
            @Override
            protected void doPerform() {
                try {
                    transactionalDataSource.getConnection();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        try {
            transactionService.execute(simple);
            fail();
        } catch (CommitException e) {
            // expected
            assertThat(e.getCause()).isEqualTo(sqlException);
        }

    }

    @Test(expected = NotInTransactionException.class)
    public void testSetRollBackOnlyWhenNotInTransactionThrowsException() {
        transactionService.setRollbackOnly();
    }


}
