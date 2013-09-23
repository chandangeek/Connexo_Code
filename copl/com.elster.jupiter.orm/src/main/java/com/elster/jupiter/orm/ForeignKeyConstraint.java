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
}
