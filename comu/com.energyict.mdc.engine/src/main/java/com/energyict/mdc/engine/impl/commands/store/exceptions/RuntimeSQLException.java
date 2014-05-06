package com.energyict.mdc.engine.impl.commands.store.exceptions;

import java.sql.SQLException;

/**
* Copyrights EnergyICT
* Date: 15/01/14
* Time: 15:12
*/
public class RuntimeSQLException extends RuntimeException {

    public RuntimeSQLException(SQLException cause) {
        super(cause);
    }

    @Override
    public synchronized SQLException getCause() {
        return (SQLException) super.getCause();
    }
}
