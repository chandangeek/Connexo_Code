/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm;

/**
 * Thrown when an operation is performed outside a Transaction, which needs to be performed from within a Transaction.
 */
public class TransactionRequiredException extends PersistenceException {
	
	private static final long serialVersionUID = 1;
	
	public TransactionRequiredException() {
		super(MessageSeeds.TRANSACTION_REQUIRED);
	}
	
}
