package com.elster.jupiter.orm;

import java.sql.SQLException;

/**
 * RuntimeException to wrap SQLExceptions
 */
public class UnderlyingSQLFailedException extends PersistenceException {
	private static final long serialVersionUID = 1L;
	
    public UnderlyingSQLFailedException(SQLException cause) {
        super(ExceptionTypes.SQL, cause);
    }
}
