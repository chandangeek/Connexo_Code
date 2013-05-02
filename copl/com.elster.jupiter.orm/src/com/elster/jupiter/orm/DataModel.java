package com.elster.jupiter.orm;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * @author kha
 * DataModel is a container for a component's table objects.
 *
 */
public interface DataModel {
	// main api
	<T, S extends T> DataMapper<T> getDataMapper(Class<T> api , Class<S> implementation, String tableName);
	
	// direct jdbc access
	Connection getConnection(boolean transactionRequired) throws SQLException;
	
	// meta data access
	String getName();
	String getDescription();	
	List<Table> getTables();	
	Table getTable(String name);
	
	// install time api
	Table addTable(String name);
	Table addTable(String schema, String tableName);
	void install(boolean executeDdl, boolean store);	
}
