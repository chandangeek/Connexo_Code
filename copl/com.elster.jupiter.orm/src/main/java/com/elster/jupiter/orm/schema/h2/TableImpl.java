package com.elster.jupiter.orm.schema.h2;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.schema.ExistingColumn;
import com.elster.jupiter.orm.schema.ExistingIndex;
import com.elster.jupiter.orm.schema.ExistingTable;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TableImpl implements ExistingTable {

    private String name;
    private List<ColumnImpl> columns = new ArrayList<>();
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
    public void addTo(DataModel dataModel, Optional<String> journalTableName) {
        Table table = dataModel.addTable(getName(), Object.class);
        if (journalTableName.isPresent()) {
            table.setJournalTableName(journalTableName.get());
        }
        for (ColumnImpl column : columns) {
            column.addTo(table);
        }
        for (ConstraintImpl constraint : constraints) {
            constraint.addTo(table);
        }
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
}
