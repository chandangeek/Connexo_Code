/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.exceptions;

import java.sql.SQLException;

public class RuntimeSQLException extends RuntimeException {

    public RuntimeSQLException(SQLException cause) {
        super(cause);
    }

    @Override
    public synchronized SQLException getCause() {
        return (SQLException) super.getCause();
    }
}
