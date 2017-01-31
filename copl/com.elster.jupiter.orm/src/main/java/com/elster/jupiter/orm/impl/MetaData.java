/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.schema.ExistingTable;
import com.elster.jupiter.util.conditions.Condition;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class MetaData {

    private final List<ExistingTable> tables;

    public MetaData(DataModel dataModel) {
        tables = dataModel.query(ExistingTable.class/*, ExistingColumn.class, ExistingConstraint.class, ExistingIndex.class*/)
                .select(Condition.TRUE)
                .stream()
                .filter(existingTable -> !existingTable.getName().startsWith("ORM_"))
                .filter(existingTable -> !existingTable.getName().startsWith("USER_"))
                .filter(existingTable -> !existingTable.getName().startsWith("FLYWAY"))
                .collect(Collectors.toList());
    }

    public List<ExistingTable> getTables() {
        return Collections.unmodifiableList(tables);
    }

    public Optional<ExistingTable> getTable(String tableName) {
        return tables.stream()
                .filter(existingTable -> existingTable.getName().equals(tableName))
                .findAny();
    }
}
