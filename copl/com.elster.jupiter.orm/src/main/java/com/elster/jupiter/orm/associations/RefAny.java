package com.elster.jupiter.orm.associations;

public interface RefAny {
	boolean isPresent();
	Object get();
	String getComponent();
	String getTableName();
	Object[] getPrimaryKey();
}
