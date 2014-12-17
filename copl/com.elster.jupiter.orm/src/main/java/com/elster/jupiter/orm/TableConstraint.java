package com.elster.jupiter.orm;

import java.util.List;

/**
 * represents a table constraint. Is either a primary key , unique or foreign key constraint
 */
public interface TableConstraint {
	String getName();
    List<? extends Column> getColumns();
	Table<?> getTable();
	boolean isPrimaryKey();
	boolean isUnique();
	boolean isForeignKey();
	boolean hasColumn(Column column);
	/*
	 * return true if all columns of the constraint are not null columns
	 */	
	boolean isNotNull();
	/*
	 * true if the constraint is only used for Object Relational Mapping,
	 * but not enforced in the RDBMS.
	 * Useful when using partitioning 
	 */
	boolean noDdl();
}
