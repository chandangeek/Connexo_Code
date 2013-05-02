package com.elster.jupiter.orm;

import java.util.*;

public interface TableConstraint {
	String getName();
	List<Column> getColumns();
	Table getTable();
	boolean isPrimaryKeyConstraint();
	boolean isUniqueConstraint();
	boolean isForeignKeyConstraint();
	boolean isNotNull();
	Object[] getColumnValues(Object value);

	// following only relevant for FK constraint
	Table getReferencedTable();	
	DeleteRule getDeleteRule();
	String getFieldName();
	String getReverseFieldName();
	String getReverseCurrentName();
}
