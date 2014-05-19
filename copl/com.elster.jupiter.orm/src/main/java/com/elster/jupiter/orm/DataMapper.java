package com.elster.jupiter.orm;

import com.elster.jupiter.util.sql.Fetcher;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.google.common.base.Optional;

import java.util.List;

/**
 * 
 * Performs persistent operations for a given type and table
 */
public interface DataMapper<T> extends Finder<T> {
	/**
	 * locks the tuple with the given primary key, using select for update
	 */
	T lock(Object... values);
	Optional<T> lockNoWait(Object... values);
	/**
	 * inserts a new tuple. Any database generated fields (e.g. generated id) will be updated in object
	 */
	void persist(T object);
	/**
	 * perform batch insert for the list of objects. Database generated fields will NOT be updated.
	 */
	void persist(List<T> objects);
	/**
	 * Updates only the columns that match with the fieldNames,
	 * or all columns if none are specified
	 */
	void update(T object, String... fieldNames);
	/**
	 * Batch update the list of objects. Database generated fields will NOT be updated
	 */
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
     *
     * @deprecated use DataModel.query() instead
	 */
	@Deprecated
	QueryExecutor<T> with(DataMapper<?> ... tupleHandlers);
	
	Optional<T> getEager(Object ... object);
	// meta data access
	Object getAttribute(Object target , String fieldName);
	
	Fetcher<T> fetcher(SqlBuilder builder);
	SqlBuilder builder(String alias, String... hints);
	
}