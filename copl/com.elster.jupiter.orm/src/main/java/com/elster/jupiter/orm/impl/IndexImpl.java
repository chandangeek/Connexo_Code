package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.IllegalTableMappingException;
import com.elster.jupiter.orm.Index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IndexImpl implements Index {

    private final String name;
    private int compress;
    private final List<ColumnImpl> columns = new ArrayList<>();
    private final TableImpl<?> table;

    IndexImpl(TableImpl<?> table, String name) {
        this.table = table;
        this.name = name;
    }


    @Override
    public List<ColumnImpl> getColumns() {
        return Collections.unmodifiableList(columns);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getCompress() {
        return compress;
    }

    private void add(Column[] columns) {
        for (Column column : columns) {
            this.columns.add((ColumnImpl) column);
        }
    }

    @Override
    public TableImpl<?> getTable() {
        return table;
    }

    boolean matches(IndexImpl toIndex) {
        if (compress != toIndex.compress) {
            return false;
        }
        if (getColumns().size() != toIndex.getColumns().size()) {
            return false;
        }
        for (int i = 0; i < getColumns().size(); i++) {
            if (!getColumns().get(i).matchesForIndex(toIndex.getColumns().get(i))) {
                return false;
            }
        }
        return true;
    }

    static class BuilderImpl implements Index.Builder {
        private IndexImpl index;

        public BuilderImpl(TableImpl<?> table, String name) {
            this.index = new IndexImpl(table, name);
        }

        @Override
        public Builder on(Column... columns) {
            for (Column column : columns) {
                if (!index.getTable().equals(column.getTable())) {
                    throw new IllegalTableMappingException("Table " + index.getTable().getName() + " : primary key can not have columns from another table : " + column.getName() + ".");
                }
            }
            index.add(columns);
            return this;
        }

        @Override
        public Builder compress(int compress) {
            this.index.compress = compress;
            return this;
        }

        @Override
        public Index add() {
            index.getTable().add(index);
            return index;
        }


    }

}
