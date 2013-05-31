package com.elster.jupiter.orm;

import java.util.List;

public interface DataMapper<T> extends Finder<T> {
	T lock(Object... values);
	void persist(T object);
	void persist(List<T> objects);
	void update(T object);
	void update(T object, String... fieldNames);
	void update(List<T> objects);
	void update(List<T> objects, String... fieldNames);
	void remove(T object);
	void remove(List<T> objects);
	QueryExecutor<T> with(DataMapper<?> ... tupleHandlers);
	Table getTable();
}