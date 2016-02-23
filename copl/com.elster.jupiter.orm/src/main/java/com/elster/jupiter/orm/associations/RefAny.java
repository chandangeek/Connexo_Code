package com.elster.jupiter.orm.associations;

/*
 * a reference to a persistent Object of unknown type
 */

import java.util.Optional;

public interface RefAny {
	boolean isPresent();
	Object get();
	Optional<?> getOptional();
	String getComponent();
	String getTableName();
	Object[] getPrimaryKey();
}
