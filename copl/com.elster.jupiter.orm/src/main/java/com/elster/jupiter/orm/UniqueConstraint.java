package com.elster.jupiter.orm;

public interface UniqueConstraint extends TableConstraint {
	interface Builder {
		Builder on(Column ... columns);
		UniqueConstraint add();
	}
}
