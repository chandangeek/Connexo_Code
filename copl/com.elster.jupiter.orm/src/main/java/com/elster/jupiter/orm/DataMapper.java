package com.elster.jupiter.orm;

import java.util.List;

import com.google.common.base.Optional;

/**
 * 
 * Performs persistent operations for a given type and table
 */
public interface DataMapper<T> extends Finder<T> {
	/**
	 * locks the tuple with the given primary key, using select for update
	 */
	T lock(Object... values);
	/**
	 * inserts a new tuple. Any database generated fields (e.g. generated id) will be updated in object
	 */
	void persist(T object);
	/**
	 * perform batch insert for the list of objects. Database generated fields will NOT be updated.
	 */
	void persist(List<T> objects);
	/**
	 * Updates all updatable columns for the tuple matching the primary key extracted from the argument. 
	 * Any database generated fields will be updated in object
	 */
	void update(T object);
	/**
	 * Updates only the columns that match with the fieldNames
	 */
	void update(T object, String... fieldNames);
	/**
	 * Batch update the list of objects. Database generated fields will NOT be updated
	 */
	void update(List<T> objects);
	void update(List<T> objects, String... fieldNames);
	/**
	 * delete the tuple whose primary key matches the primary key extracted from the argument
	 */
	void remove(T object);
	/**
	 * batch delete
	 */
	void remove(List<T> objects);
	/**
	 * create a query executor that can join with the tables served by the arguments
	 * There must be a (direct or indirect) foreign key relationship between the receiver's type and the 
	 * types mapped by the arguments. 
	 */
	QueryExecutor<T> with(DataMapper<?> ... tupleHandlers);
	/**
	 * @return the table served by the receiver
	 */
	Table getTable();
	Object[] getPrimaryKey(T object);
	Optional<T> getEager(Object ... object);
	// meta data access
	Object getAttribute(Object target , String fieldName);
}