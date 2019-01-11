/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.transaction.impl;

import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionEvent;

import java.util.HashMap;
import java.util.Map;

public class TransactionContextImpl implements TransactionContext {

	private TransactionServiceImpl service;
	private TransactionEvent stats;
    private Map<String, Object> properties = new HashMap<>();
	
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

    @Override
    public void setProperty(String name, Object value) {
        properties.put(name, value);
    }

    @Override
    public Object getProperty(String name) {
        return properties.get(name);
    }

}
