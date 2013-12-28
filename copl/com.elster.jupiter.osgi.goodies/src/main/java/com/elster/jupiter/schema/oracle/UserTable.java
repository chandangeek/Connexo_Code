package com.elster.jupiter.schema.oracle;

import java.util.List;

public interface UserTable {
	String getName();
	List<? extends UserColumn> getColumns();
	List<String> generate();
}
