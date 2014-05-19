package com.elster.jupiter.util.sql;

import java.util.Iterator;

public interface QueryIterator<T> extends Iterator<T> , AutoCloseable {
	@Override
	void close();
}
