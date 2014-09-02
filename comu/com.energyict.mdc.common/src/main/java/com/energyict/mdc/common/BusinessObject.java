package com.energyict.mdc.common;

import java.sql.SQLException;

/**
 * BusinessObject defines the minimum contract for a business object
 */
public interface BusinessObject extends BusinessObjectProxy {

    /**
     * Removes this business object from the application.
     *
     * @throws BusinessException if a business exception occurred.
     * @throws SQLException      if a database error occurred.
     */
    public void delete() throws BusinessException, SQLException;

    /**
     * Indicates if  this business object can be removed from the application.
     *
     * @return true if it can be removed, false if not.
     */
    public boolean canDelete();

    /**
     * Return the <code>BusinessObjectFactory<code> used to create it.
     *
     * @return a BusinessObjectFactory.
     * @see BusinessObjectFactory
     */
    public BusinessObjectFactory getFactory();

}