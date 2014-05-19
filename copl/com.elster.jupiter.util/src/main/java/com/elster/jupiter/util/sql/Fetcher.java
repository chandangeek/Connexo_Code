package com.elster.jupiter.util.sql;

public interface Fetcher<T> extends Iterable<T> , AutoCloseable {
	@Override
	void close();
}
