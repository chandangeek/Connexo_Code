package com.elster.jupiter.orm;

import java.util.List;

public interface Component {
	String getName();
	String getDescription();	
	List<Table> getTables();
	Table getTable(String name);
	
	// install time api
	void setDescription(String description);
	Table addTable(String name);
	Table addTable(String schema, String tableName);
	
}
