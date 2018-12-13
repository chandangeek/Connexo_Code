/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.transaction;

import com.elster.jupiter.util.exception.BaseException;

import java.sql.SQLException;

/**
 * Thrown when a database commit fails, throwing a SQLException.
 */
public class CommitException extends BaseException {
	
	private static final long serialVersionUID = 1;

    /**
     * @param ex the cause
     */
	public CommitException(SQLException ex) {
		super(ExceptionTypes.COMMIT_FAILED, ex);
	}
}
