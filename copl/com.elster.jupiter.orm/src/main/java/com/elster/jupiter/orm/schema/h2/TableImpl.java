/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.schema.h2;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.schema.ExistingColumn;
import com.elster.jupiter.orm.schema.ExistingIndex;
import com.elster.jupiter.orm.schema.ExistingTable;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.Checks.is;

public class TableImpl implements ExistingTable {

    private String name;
    private List<ColumnImpl> columns = new ArrayList<>();
    private List<IndexColumnImpl> indexColumns = new ArrayList<>();
    private List<ConstraintImpl> constraints = new ArrayList<>();

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<ColumnImpl> getColumns() {
        return ImmutableList.copyOf(columns);
    }

    public List<ConstraintImpl> getConstraints() {
        return ImmutableList.copyOf(constraints);
    }

    public ColumnImpl getColumn(String name) {
        for (ColumnImpl column : columns) {
            if (column.getName().equals(name)) {
                return column;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Table: " + name;
    }

    @Override
    public void addColumnsTo(DataModel dataModel, String journalTableName) {
        Table table = getOrAddTable(dataModel);
        if (!is(journalTableName).emptyOrOnlyWhiteSpace()) {
            table.setJournalTableName(journalTableName);
        }
        for (ColumnImpl column : columns) {
            column.addTo(table);
        }
    }

    @Override
    public void addConstraintsTo(DataModel dataModel) {
        Table table = getOrAddTable(dataModel);
        for (ConstraintImpl constraint : constraints) {
            constraint.addTo(table);
        }
    }

    private Table getOrAddTable(DataModel dataModel) {
        Table table = dataModel.getTable(getName());
        if (table == null) {
            table = dataModel.addTable(getName(), Object.class);
        }
        return table;
    }

    public List<ExistingColumn> getPrimaryKeyColumns() {
        for (ConstraintImpl constraint : constraints) {
            if (constraint.isPrimaryKey()) {
                return constraint.getColumns();
            }
        }
        return Collections.emptyList();
    }

    @Override
    public List<? extends ExistingIndex> getIndexes() {
        return Collections.emptyList();
    }

    public List<ExistingColumn> getIndexColumns(String indexName) {
        return indexColumns.stream()
                .filter(indexColumn -> indexColumn.getIndexName().equals(indexName))
                .sorted(Comparator.comparing(IndexColumnImpl::getPosition))
                .map(IndexColumnImpl::getColumn)
                .collect(Collectors.toList());
    }
}
