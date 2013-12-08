package com.elster.jupiter.orm;

public interface PrimaryKeyConstraint extends TableConstraint {
	interface Builder {
		Builder on(Column ... columns);
		PrimaryKeyConstraint add();
	}
}
