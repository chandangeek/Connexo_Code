package com.elster.jupiter.orm;

/*
 * represents a unique constraint
 */
public interface UniqueConstraint extends TableConstraint {
	interface Builder {
		Builder on(Column ... columns);
		UniqueConstraint add();
	}
}
