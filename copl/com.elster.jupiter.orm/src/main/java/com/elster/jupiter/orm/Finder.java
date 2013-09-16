package com.elster.jupiter.orm;

import com.google.common.base.Optional;

import java.util.List;
import java.util.Map;

/**
 * 
 * Fetches tuples from the database and converts them to objects
 * A Finder is associated with one table. 
 * Finder contains simple find methods. More advanced criteria can be specified on a Query object.
 * Also a Query object can join in other tables to avoid the N+1 ORM performance issue
 */
public interface Finder<T> {
	/**
	 * 
	 * Fetch all tuples. Only use on small tables
	 */
    List<T> find();
    /**
     * Fetch all tuples where fieldName equals value
     */
    List<T> find(String fieldName, Object value);
    /**
     * 
     * @param fieldName
     * @param value
     * @param orderBy if this matches a fieldName, it is converted to the corresponding Column name, 
     * 	otherwise the string is passed transparently to the order by clause on the select statement.
     * @return
     */
    List<T> find(String fieldName, Object value, String orderBy);
    /**
     * Fetch all tuples where fieldName1 equals value 1 AND fieldName 2 equals value2 
     * @param fieldName1
     * @param value1
     * @param fieldName2
     * @param value2
     * @return
     */
    List<T> find(String fieldName1, Object value1,String fieldName2, Object value2);
    List<T> find(String fieldName1, Object value1,String fieldName2, Object value2, String orderBy);    
    List<T> find(String[] fieldNames , Object[] values );
    List<T> find(String[] fieldNames , Object[] values , String... orderBy);
    List<T> find(Map<String,Object> valueMap);
    List<T> find(Map<String,Object> valueMap,String... orderBy);
	/**
	 * Find object by primary key
	 * @param values
	 * @return
	 */
	Optional<T> get(Object... values);
	/**
	 * Find journal entries for a primary key
	 */
    List<T> getJournal(Object... values);
    /**
     * Finde object by primary key
     * @throws DoesNotExistException if not found
     */
    T getExisting(Object... values);
    /**
     * Currently the getUnique methods do not check if the resultset contains exactly one tuple
     * This may change
     * @return may be null   
     */
	Optional<T> getUnique(String columnName, Object value);
	Optional<T> getUnique(String columnName1, Object value1, String columnName2,Object value2);
	Optional<T> getUnique(String[] fieldNames , Object[] values);
	
}