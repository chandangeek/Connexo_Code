/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.transaction;

public interface TransactionContext extends AutoCloseable {
	public void close();
	public void commit();
	public TransactionEvent getStats();

    default void setProperty(String name, Object value) {
        throw new UnsupportedOperationException();
    }

    default Object getProperty(String name) {
        throw new UnsupportedOperationException();
    }
}
