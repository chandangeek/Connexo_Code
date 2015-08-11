package com.elster.jupiter.orm;

import aQute.bnd.annotation.ProviderType;

/*
 * Models a primary key definition.
 */
@ProviderType
public interface PrimaryKeyConstraint extends TableConstraint {

	/*
	 * indicates whether 0 is allowed if the primary key consist of a single long field
	 */
	boolean allowZero();

	@ProviderType
	interface Builder {
		Builder on(Column ... columns);
		Builder allowZero();
		PrimaryKeyConstraint add();
	}
}
