package com.elster.jupiter.orm;

public interface PrimaryKeyConstraint extends TableConstraint {
	
	boolean allowZero();
	
	interface Builder {
		Builder on(Column ... columns);
		Builder allowZero();
		PrimaryKeyConstraint add();
	}
}
