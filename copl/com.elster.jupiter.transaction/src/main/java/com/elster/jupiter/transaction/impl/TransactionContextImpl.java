/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.transaction.impl;

import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionEvent;

public class TransactionContextImpl implements TransactionContext {

	private TransactionServiceImpl service;
	private TransactionEvent stats;
	
	public TransactionContextImpl(TransactionServiceImpl service) {
		this.service = service;
	}

	@Override
	public void close() {
		if (service != null) {
			service.rollback();
		}
	}

	@Override
	public void commit() {
		try {
			stats = service.commit();
		} finally {
			service = null;
		}
	}

	@Override
	public TransactionEvent getStats() {
		if (service != null) {
			throw new IllegalStateException("Transaction not finished");
		}
		return stats;
	}

}
