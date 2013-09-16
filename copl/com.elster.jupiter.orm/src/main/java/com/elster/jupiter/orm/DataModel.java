package com.elster.jupiter.orm;

import java.security.Principal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author kha
 * DataModel is a container for a component's table description objects.
 *
 */
public interface DataModel {
	/** 
	 * Create a DataMapper for the given arguments. Used when all tuples in the table are mapped to instances of the same class
	 */
	<T> DataMapper<T> getDataMapper(Class<T> api , Class<? extends T> implementation, String tableName);
	/**
	 * Create a DataMapper for the given arguments. Used with "single table inheritance".
	 * Tuples of the table are mapped to different classes, based on a differentiator column
	 */
	<T> DataMapper<T> getDataMapper(Class<T> api , Map<String,Class <? extends T>> implementations, String tableName);
	/**
	 * Utility method to obtain the JDBC connection associated with the thread. 
	 */
	Connection getConnection(boolean transactionRequired) throws SQLException;
	/**
	 * Utility method to obtain the Principal associated with the thread.
	 */
	Principal getPrincipal();
	
	String getName();
	String getDescription();
    List<Table> getTables();
    Table getTable(String name);
    /**
     * Adds a table to the DataModel. ORM will use the default schema (defined by the DataSource user property)
     */
	Table addTable(String name);
	/**
     * Adds a table to the DataModel in the specified schema
     */	
	Table addTable(String schema, String tableName);
	/**
	 * Installs the DataModel
	 * @param executeDdl: if true, execute the DDL to create the tables in the database
	 * @param store: if true, store the mappings in the ORM tables
	 */
	void install(boolean executeDdl, boolean store);	
}
