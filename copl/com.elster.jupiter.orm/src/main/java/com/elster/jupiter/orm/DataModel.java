package com.elster.jupiter.orm;

import java.security.Principal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author kha
 * DataModel is a container for a component's table objects.
 *
 */
public interface DataModel {
	// main api
	<T> DataMapper<T> getDataMapper(Class<T> api , Class<? extends T> implementation, String tableName);
	<T> DataMapper<T> getDataMapper(Class<T> api , Map<String,Class <? extends T>> implementations, String tableName);
	
	// direct jdbc access
	Connection getConnection(boolean transactionRequired) throws SQLException;
	// courtesy method to avoid binding to threadPrincipal service
	Principal getPrincipal();
	
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
