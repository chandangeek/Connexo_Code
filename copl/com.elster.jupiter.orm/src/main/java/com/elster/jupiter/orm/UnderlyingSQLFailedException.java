package com.elster.jupiter.orm;

import java.sql.SQLException;

/**
 * RuntimeException to wrap SQLExceptions
 */
public class UnderlyingSQLFailedException extends PersistenceException {

    public UnderlyingSQLFailedException(SQLException cause) {
        super(ExceptionTypes.SQL, cause);
    }
}
