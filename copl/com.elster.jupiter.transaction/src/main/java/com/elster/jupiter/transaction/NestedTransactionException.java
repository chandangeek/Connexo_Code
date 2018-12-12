/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.transaction;

import com.elster.jupiter.util.exception.BaseException;

/**
 * Thrown when attempting to execute a nested transaction.
 */
public class NestedTransactionException extends BaseException {
	
	private static final long serialVersionUID = 1;
	
	public NestedTransactionException() {
        super(ExceptionTypes.NESTED_TRANSACTION);
	}
}
