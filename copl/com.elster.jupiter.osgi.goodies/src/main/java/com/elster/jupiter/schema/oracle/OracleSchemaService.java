package com.elster.jupiter.schema.oracle;

import com.elster.jupiter.orm.schema.ExistingTable;
import java.util.Optional;

import java.util.List;

public interface OracleSchemaService {

    List<ExistingTable> getTableNames();

    Optional<ExistingTable> getTable(String tableName);
}
