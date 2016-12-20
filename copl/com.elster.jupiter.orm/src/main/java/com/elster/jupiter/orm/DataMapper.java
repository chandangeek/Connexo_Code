package com.elster.jupiter.orm;

import com.elster.jupiter.util.sql.Fetcher;
import com.elster.jupiter.util.sql.SqlBuilder;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 
 * Performs persistent operations for a given type and table
 */
@ProviderType
public interface DataMapper<T> extends Finder<T> {
	/**
	 * locks the tuple with the given primary key, using select for update
	 */
	T lock(Object... values);

	/**
	 * Locks the object with the given primary key (values) if it has the passed version in the database.
	 * If the object exists and it has the passed version, the object is returned and the tuple is locked in the database.
	 * This method must be called within a Transaction
	 *
	 * @param version              the current version of the object to lock
	 * @param primaryKeyComposites the identitifiers that together compose the primary key
	 * @return the object identified by the primaryKeyComposites and that has the passed version.
	 * 			If the object did not have the passed version, or no object with the passed primaryKey exists, Optional.emtpy()
	 * @since 1.2
	 */
	Optional<T> lockObjectIfVersion(long version, Object... primaryKeyComposites);

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
	void remove(List<? extends T> objects);

	Optional<T> getEager(Object ... object);
	// meta data access
	Object getAttribute(Object target , String fieldName);
	
	Fetcher<T> fetcher(SqlBuilder builder);

	SqlBuilder builder(String alias, String... hints);

    Set<String> getQueryFields();
}
