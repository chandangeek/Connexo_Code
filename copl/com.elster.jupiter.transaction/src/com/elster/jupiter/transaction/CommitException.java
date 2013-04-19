package com.elster.jupiter.transaction;

import java.sql.SQLException;

public class CommitException extends RuntimeException {
	
	private static final long serialVersionUID = 1;
	
	public CommitException(SQLException ex) {
		super(ex);
	}
}
