/*
 * Transaction.java
 *
 * Created on 21 november 2001, 14:05
 */

package com.energyict.mdc.common;

import java.sql.SQLException;

/**
 * Transaction implementations encapsulate code that
 * should be executed in a single unit of work.
 * When executing a transaction,
 * the framework code will
 * <UL>
 * <LI>set up a transaction context</LI>
 * <LI>call the transaction's doExecute method</LI>
 * <LI>commit or rollback the transaction, depending on
 * doExecute throwing an exception</LI>
 * </UL>
 *
 * @author Karel
 */
public interface Transaction<T> {

    /**
     * executes the transactional code.
     *
     * @return the tranaction's result or null.
     * @throws BusinessException if a business error occured
     * @throws SQLException      if a database error occured
     */
    public T doExecute() throws BusinessException, SQLException;
}
