package com.elster.jupiter.orm;

import aQute.bnd.annotation.ProviderType;

/*
 * Models a foreign key.
 */
@ProviderType
public interface ForeignKeyConstraint extends TableConstraint {
	Table<?> getReferencedTable();
	DeleteRule getDeleteRule();
	String getFieldName();
	String getReverseFieldName();
	String getReverseOrderFieldName();
	String getReverseCurrentFieldName();
	boolean isComposition();
	boolean isOneToOne();
	boolean isRefPartition();

	@ProviderType
	interface Builder {
		Builder on(Column ... columns);
		Builder map(String field);
		Builder map(String field, Class<?> eager, Class<?>... eagers);
		Builder onDelete(DeleteRule rule);
		Builder references(String tableName);

		/**
		 * Specify that the foreign key under construction
		 * references the table managed by the component
		 * with the specified name
		 *
		 * @param componentName The name of the component that manages the table
		 * @param tableName The name of the table
		 * @return The Builder
		 * @deprecated Use #references(Class apiClass) instead
		 */
		@Deprecated
		Builder references(String componentName , String tableName);
		Builder references(Class apiClass);
		Builder reverseMap(String field);
		Builder reverseMap(String field, Class<?> eager, Class<?> ... eagers);
		Builder reverseMapOrder(String field);
		Builder reverseMapCurrent(String field);
		Builder composition();
		Builder refPartition();
		Builder noDdl();
		ForeignKeyConstraint add();
	}

}
