package com.energyict.mdc.common;

import java.sql.SQLException;

/**
 * Class DatabaseException is an unchecked exception wrapper for a SQLException.
 */
public class DatabaseException extends ApplicationException {

    private static final int RESOURCE_BUSY_ORACLE_ERROR_CODE = 54;

    /**
     * Creates a new instance
     *
     * @param ex the cause
     */
    public DatabaseException (SQLException ex) {
        super(ex);
    }

    public boolean isResourceBusy () {
        return this.isResourceBusy(getSqlCause(getCause()));
    }

    private SQLException getSqlCause (Throwable ex) {
        if (getCause() instanceof SQLException) {
            return (SQLException) ex;
        }
        return null;
    }

    private boolean isResourceBusy (SQLException ex) {
        return ex != null && ex.getErrorCode() == RESOURCE_BUSY_ORACLE_ERROR_CODE;
    }

}

