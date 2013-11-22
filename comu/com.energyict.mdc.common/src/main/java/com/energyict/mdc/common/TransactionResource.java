package com.energyict.mdc.common;

import java.sql.SQLException;

/**
 * TransactionResources participate in Transaction Management across different resources
 */
public interface TransactionResource {

    /**
     * Indicates a new transaction has started.
     *
     * @throws SQLException If a sql error occurred.
     */
    public void begin() throws SQLException;

    /**
     * Indicates the current transaction is committed.
     * is called directly after the corresponding database transaction is commited.
     *
     * @throws SQLException If a sql error occurred.
     */
    public void commit() throws SQLException;

    /**
     * Indicates the current transaction will be committed.
     * is called immediately before the corresponding database transaction is commited.
     * if any resources throws an exception, the transaction will be rollbacked.
     *
     * @throws SQLException If a sql error occurred.
     */
    public void prepare() throws SQLException;

    /**
     * Indicates the current transaction is rollbacked.
     * is called immediately after the corresponding database transaction has been rollbacked.
     *
     * @throws SQLException If a sql error occurred.
     */
    public void rollback() throws SQLException;
}
