package com.energyict.mdc.common.impl;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.DatabaseException;
import com.energyict.mdc.common.Transaction;
import com.energyict.mdc.common.TransactionResource;
import com.energyict.mdc.common.TransactionSequenceException;

import com.elster.jupiter.transaction.TransactionService;

import java.sql.SQLException;

/**
 * @author Karel
 */
public class TransactionContext {

    private int nestCount = 0;

    public TransactionContext() {
        super();
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
            if (success) {
                resource.commit();
            }
            else {
                this.doRollback(resource);
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
    }

    public boolean isFinished () {
        return this.nestCount == 0;
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
                            return transaction.doExecute();
                        }
                        catch (BusinessException e) {
                            throw new RuntimeBusinessException(e);
                        }
                        catch (SQLException e) {
                            throw new RuntimeSQLException(e);
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