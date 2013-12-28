package com.elster.jupiter.schema.oracle;

import java.util.List;

import com.google.common.base.Optional;

public interface OracleSchemaService {

	List<UserTable> getTableNames();
	Optional<UserTable> getTable(String tableName);

}
