package com.elster.jupiter.orm;

import java.util.*;

public interface TableConstraint {
	String getName();
	List<Column> getColumns();
	Table getTable();
	Table getReferencedTable();	
	DeleteRule getDeleteRule();
	String getComponentName();
	String getTableName();
	boolean isPrimaryKeyConstraint();
	boolean isUniqueConstraint();
	boolean isForeignKeyConstraint();
	String getFieldName();
	String getReverseFieldName();
	boolean isNotNull();
}
