package com.elster.jupiter.orm;

import java.util.*;

public interface TableConstraint {
	String getName();
	List<Column> getColumns();
	Table getTable();
	boolean isPrimaryKey();
	boolean isUnique();
	boolean isForeignKey();
	boolean isNotNull();
	Object[] getColumnValues(Object value);
}
