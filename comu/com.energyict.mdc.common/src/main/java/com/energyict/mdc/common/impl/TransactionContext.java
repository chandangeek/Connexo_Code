package com.energyict.mdc.common.impl;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.DatabaseException;
import com.energyict.mdc.common.JmsSessionContext;
import com.energyict.mdc.common.Transaction;
import com.energyict.mdc.common.TransactionResource;
import com.energyict.mdc.common.TransactionSequenceException;
import com.elster.jupiter.transaction.TransactionService;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Karel
 */
public class TransactionContext {

    private int nestCount = 0;
    private JmsSessionContext jmsSessionContext;
    private DataSource dataSource;
    private Connection connection;

    public TransactionContext (DataSource dataSource) {
        super();
        this.dataSource = dataSource;
    }

    public boolean begin () throws SQLException {
        this.checkLevel();
        this.nestCount++;
        return this.nestCount == 1;
    }

    private void checkLevel () {
        if (this.nestCount <= 0) {
            throw new TransactionSequenceException("Invalid nest count: " + this.nestCount);
        }
    }

    private boolean commit (TransactionResource resource) throws SQLException {
        this.checkLevel();
        this.nestCount--;
        if (this.nestCount == 0) {
            doCommit(resource);
        }
        return this.nestCount == 0;
    }

    private void doCommit (TransactionResource resource) throws SQLException {
        boolean success = false;
        try {
            resource.prepare();
            success = true;
        }
        finally {
            try {
                if (success) {
                    resource.commit();
                }
                else {
                    this.doRollback(resource);
                }
            }
            finally {
                if (jmsSessionContext != null) {
                    jmsSessionContext.close();
                    jmsSessionContext = null;
                }
            }
        }
    }

    private void rollback (TransactionResource resource) throws SQLException {
        try {
            this.doRollback(resource);
        }
        finally {
            this.nestCount--;
        }
    }

    private void doRollback (TransactionResource resource) throws SQLException {
        resource.rollback();
    }

    public void close (TransactionResource resource) {
        if (!this.isFinished()) {
            try {
                this.rollback(resource);
            }
            catch (SQLException e) {
                throw new DatabaseException(e);
            }
        }
        this.closeConnection();
    }

    public boolean isFinished () {
        return this.nestCount == 0;
    }

    public JmsSessionContext getJmsSessionContext () {
        if (jmsSessionContext == null) {
            jmsSessionContext = new JmsSessionContext();
        }
        return jmsSessionContext;
    }

    public Connection getConnection () {
        if (this.isFinished() && ((this.connection == null || this.isClosed(this.connection)))) {
            this.obtainConnection();
        }
        return this.connection;
    }

    private boolean isClosed(Connection connection) {
        try {
            return connection.isClosed();
        }
        catch (SQLException e) {
            // Really?
            throw new DatabaseException(e);
        }
    }

    public Connection getRelationConnection () {
        try {
            return this.dataSource.getConnection();
        }
        catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public <T> T execute (Transaction<T> transaction, TransactionService transactionService, TransactionResource resource) throws SQLException, BusinessException {
        T result = null;
        boolean success = false;
        try {
            this.nestCount++;
            result = this.doExecute(transaction, transactionService);
            success = true;
        }
        finally {
            if (success) {
                this.commit(resource);
            }
            else {
                this.rollback(resource);
            }
        }
        return result;
    }

    private <T> T doExecute (final Transaction<T> transaction, TransactionService transactionService) throws BusinessException, SQLException {
        if (this.nestCount == 1) {
            // Top level transaction
            try {
                return transactionService.execute(new com.elster.jupiter.transaction.Transaction<T>() {
                    @Override
                    public T perform () {
                        try {
                            obtainConnection();
                            return transaction.doExecute();
                        }
                        catch (BusinessException e) {
                            throw new RuntimeBusinessException(e);
                        }
                        catch (SQLException e) {
                            throw new RuntimeSQLException(e);
                        }
                        finally {
                            closeConnection();
                        }
                    }
                });
            }
            catch (RuntimeBusinessException e) {
                throw e.getCause();
            }
            catch (RuntimeSQLException e) {
                throw e.getCause();
            }
        }
        else {
            // Nested transaction
            return transaction.doExecute();
        }
    }

    private void obtainConnection () {
        try {
            this.connection = this.dataSource.getConnection();
        }
        catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    void closeConnection () {
        try {
            if (this.connection != null) {
                /* Close the Jupiter connection that was created
                 * for the purpose of this transaction. */
                this.connection.close();
                this.connection = null;
            }
        }
        catch (SQLException e) {
            throw new RuntimeSQLException(e);
        }
    }

    private class RuntimeBusinessException extends RuntimeException {
        private RuntimeBusinessException (BusinessException cause) {
            super(cause);
        }

        @Override
        public synchronized BusinessException getCause () {
            return (BusinessException) super.getCause();
        }
    }

    private class RuntimeSQLException extends RuntimeException {
        private RuntimeSQLException (SQLException cause) {
            super(cause);
        }

        @Override
        public synchronized SQLException getCause () {
            return (SQLException) super.getCause();
        }
    }

}