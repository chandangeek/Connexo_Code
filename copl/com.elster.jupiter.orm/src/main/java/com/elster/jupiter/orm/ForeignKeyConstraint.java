package com.elster.jupiter.orm;

public interface ForeignKeyConstraint extends TableConstraint {
	Table getReferencedTable();	
	DeleteRule getDeleteRule();
	String getFieldName();
	String getReverseFieldName();
	String getReverseOrderFieldName();
	String getReverseCurrentFieldName();
	/*
	 * returns true if this is one to one relation,
	 * instead of the usual 1 to n
	 */
	boolean isOneToOne();
	
	interface Builder {
		Builder on(Column ... columns);
		Builder map(String field);
		Builder onDelete(DeleteRule rule);
		Builder references(String tableName);
		Builder references(String componentName , String tableName);
		Builder reverseMap(String field);
		Builder reverseMapOrder(String field);
		Builder reverseMapCurrent(String field);
		ForeignKeyConstraint add();
	}
}
