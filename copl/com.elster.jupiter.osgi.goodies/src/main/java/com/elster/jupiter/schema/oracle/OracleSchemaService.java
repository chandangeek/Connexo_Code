/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.schema.oracle;

import com.elster.jupiter.orm.schema.ExistingTable;
import java.util.Optional;

import java.util.List;

public interface OracleSchemaService {

    List<ExistingTable> getTableNames();

    Optional<ExistingTable> getTable(String tableName);
}
