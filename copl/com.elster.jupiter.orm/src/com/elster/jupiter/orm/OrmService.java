package com.elster.jupiter.orm;

import java.sql.*;

public interface OrmService {
	// standard api
	<T,S extends T> DataMapper<T> getDataMapper(Class<T> api , Class<S> implementation , String componentName , String tableName );
	//direct jdbc access
	Connection getConnection(boolean transactionRequired) throws SQLException;
	// meta data access
	Component getComponent(String componentName);
	Table getTable(String componentName, String tableName);
	// api for modules with dynamic orm mapping
	Component newComponent(String name, String description);
	boolean add(Component component);	
	// install time api
	void install(Component component, boolean executeDdl,boolean storeMappings);
	void install(boolean executeDdl, boolean storeMappings);
}