/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm;

import com.elster.jupiter.util.exception.PersistenceException;

import java.sql.SQLException;

/**
 * RuntimeException to wrap SQLExceptions
 */
public class UnderlyingSQLFailedException extends PersistenceException {
	private static final long serialVersionUID = 1L;
	
    public UnderlyingSQLFailedException(SQLException cause) {
        super(MessageSeeds.SQL, cause);
    }
}
