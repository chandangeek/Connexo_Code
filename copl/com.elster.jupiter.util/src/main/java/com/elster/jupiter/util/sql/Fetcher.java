/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.sql;

public interface Fetcher<T> extends Iterable<T> , AutoCloseable {
	@Override
	void close();
}
