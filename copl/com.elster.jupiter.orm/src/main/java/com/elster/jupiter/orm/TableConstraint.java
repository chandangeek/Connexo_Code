package com.elster.jupiter.orm;

import java.util.List;

/**
 * represents a table constraint. Is either a primary key , unique or foreign key constraint
 */
public interface TableConstraint {
	String getName();
    List<Column> getColumns();
	Table getTable();
	boolean isPrimaryKey();
	boolean isUnique();
	boolean isForeignKey();
	/**
	 * 
	 * @return false if at least one of the columns is nullable, true otherwise
	 */
	boolean isNotNull();
	Object[] getColumnValues(Object value);
}
