package com.elster.jupiter.transaction;

import java.sql.SQLException;

/**
 * Thrown when a database commit fails, throwing a SQLException.
 */
public class CommitException extends RuntimeException {
	
	private static final long serialVersionUID = 1;

    /**
     * @param ex the cause
     */
	public CommitException(SQLException ex) {
		super(ex);
	}
}
