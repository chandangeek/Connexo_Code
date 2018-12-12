/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.SortedSet;

/**
 * Models a table constraint. Is either a primary key, unique or foreign key constraint.
 */
@ProviderType
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

	boolean isInVersion(Version version);

	SortedSet<Version> changeVersions();
}
