package com.elster.jupiter.transaction.impl;

import java.sql.SQLException;

import com.elster.jupiter.transaction.CommitException;
import com.elster.jupiter.transaction.TransactionContext;

public class TransactionContextImpl implements TransactionContext {

	private TransactionServiceImpl service;
	
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
			service.commit();
		} finally {
			service = null;
		}
	}

}
