package com.elster.jupiter.orm;

/*
 * describes a foreign key
 */
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
	
	interface Builder {
		Builder on(Column ... columns);
		Builder map(String field);
		Builder map(String field, Class<?> eager, Class<?>... eagers);
		Builder onDelete(DeleteRule rule);
		Builder references(String tableName);
		Builder references(String componentName , String tableName);
		Builder reverseMap(String field);
		Builder reverseMap(String field, Class<?> eager, Class<?> ... eagers);
		Builder reverseMapOrder(String field);
		Builder reverseMapCurrent(String field);
		Builder composition();
		Builder refPartition();
		ForeignKeyConstraint add();		
	}

}
