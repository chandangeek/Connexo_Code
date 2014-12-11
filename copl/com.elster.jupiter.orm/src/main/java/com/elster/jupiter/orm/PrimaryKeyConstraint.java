package com.elster.jupiter.orm;

/*
 * represents a primary key definition
 */
public interface PrimaryKeyConstraint extends TableConstraint {
	
	/*
	 * indicates whether 0 is allowed if the primary key consist of a single long field
	 */	
	boolean allowZero();
	
	interface Builder {
		Builder on(Column ... columns);
		Builder allowZero();
		PrimaryKeyConstraint add();
	}
}
