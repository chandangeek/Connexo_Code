package com.elster.jupiter.orm.associations;

/*
 * a reference to a persistent Object of unknown type
 */

public interface RefAny {
	boolean isPresent();
	Object get();
	String getComponent();
	String getTableName();
	Object[] getPrimaryKey();
}
