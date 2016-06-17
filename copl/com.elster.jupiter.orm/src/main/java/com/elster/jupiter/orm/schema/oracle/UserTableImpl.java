package com.elster.jupiter.orm.schema.oracle;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.schema.ExistingColumn;
import com.elster.jupiter.orm.schema.ExistingTable;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.elster.jupiter.util.Checks.is;

public class UserTableImpl implements ExistingTable {

    private String name;
    private List<UserColumnImpl> columns = new ArrayList<>();
    private List<UserConstraintImpl> constraints = new ArrayList<>();
    private List<UserIndexImpl> indexes = new ArrayList<>();

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<UserColumnImpl> getColumns() {
        return ImmutableList.copyOf(columns);
    }

    public List<UserConstraintImpl> getConstraints() {
        return ImmutableList.copyOf(constraints);
    }

    public UserColumnImpl getColumn(String name) {
        for (UserColumnImpl column : columns) {
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

    private Table getOrAddTable(DataModel dataModel) {
        Table table = dataModel.getTable(getName());
        if (table == null) {
            table = dataModel.addTable(getName(), Object.class);
        }
        return table;
    }

    @Override
    public void addColumnsTo(DataModel dataModel, String journalTableName) {
        Table table = getOrAddTable(dataModel);
        if (!is(journalTableName).emptyOrOnlyWhiteSpace()) {
            table.setJournalTableName(journalTableName);
        }
        for (UserColumnImpl column : columns) {
            column.addTo(table);
        }
    }

    @Override
    public void addConstraintsTo(DataModel dataModel) {
        Table table = getOrAddTable(dataModel);
        for (UserConstraintImpl constraint : constraints) {
            constraint.addTo(table);
        }
        for (UserIndexImpl index : indexes) {
            index.addTo(table);
        }
    }

    public List<ExistingColumn> getPrimaryKeyColumns() {
        for (UserConstraintImpl constraint : constraints) {
            if (constraint.isPrimaryKey()) {
                return constraint.getColumns();
            }
        }
        return Collections.emptyList();
    }

    @Override
    public List<UserIndexImpl> getIndexes() {
        return ImmutableList.copyOf(indexes);
    }
}
