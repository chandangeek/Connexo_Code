/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.transaction;

import com.elster.jupiter.util.exception.BaseException;

/**
 * Thrown when a transaction related operation is performed outside of a transaction.
 */
public class NotInTransactionException extends BaseException {
	
	private static final long serialVersionUID = 1;
	
	public NotInTransactionException() {
        super(ExceptionTypes.NOT_IN_TRANSACTION);
	}
}
