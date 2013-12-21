package com.elster.jupiter.orm;

import java.util.List;

/**
 * represents a table constraint. Is either a primary key , unique or foreign key constraint
 */
public interface TableConstraint {
	String getName();
    List<? extends Column> getColumns();
	Table getTable();
	boolean isPrimaryKey();
	boolean isUnique();
	boolean isForeignKey();
	boolean hasColumn(Column column);
	boolean isNotNull();
}
