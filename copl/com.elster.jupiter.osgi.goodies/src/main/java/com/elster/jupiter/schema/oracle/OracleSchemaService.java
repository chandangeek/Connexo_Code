package com.elster.jupiter.schema.oracle;

import com.elster.jupiter.orm.schema.ExistingTable;
import com.google.common.base.Optional;

import java.util.List;

public interface OracleSchemaService {

    List<ExistingTable> getTableNames();

    Optional<ExistingTable> getTable(String tableName);
}
