package com.elster.jupiter.orm;

import aQute.bnd.annotation.ProviderType;

/*
 * Models a unique constraint.
 */
@ProviderType
public interface UniqueConstraint extends TableConstraint {
	@ProviderType
	interface Builder {
		Builder on(Column ... columns);
		UniqueConstraint add();
	}
}
