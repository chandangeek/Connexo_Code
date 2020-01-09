package com.energyict.mdc.engine.offline.core.exception;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.engine.offline.core.Utils;

import java.sql.SQLException;

/**
 * Class DatabaseException is an unchecked exception wrapper for a SQLException.
 */
public class DatabaseException extends ApplicationException {

    /**
     * Creates a new instance
     *
     * @param ex the cause
     */
    public DatabaseException(SQLException ex) {
        super(ex);
    }


    public boolean isResourceBusy() {
        SQLException ex = getSqlCause(getCause());
        return Utils.isResourceBusy(ex);
    }

    private SQLException getSqlCause(Throwable ex) {
        if (getCause() instanceof SQLException) {
            return (SQLException) ex;
        }
        return null;
    }
}


