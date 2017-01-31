/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.schema.oracle;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Index;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.schema.ExistingIndex;

import java.util.ArrayList;
import java.util.List;

public class UserIndexImpl implements ExistingIndex {

    private String name;
    private int compression;
    private String type;

    private Reference<UserTableImpl> table = ValueReference.absent();
    private List<UserIndexColumnImpl> columns = new ArrayList<>();

    @Override
    public String getName() {
        return name;
    }

    UserTableImpl getTable() {
        return table.get();
    }

    public void addTo(Table<?> table) {
        if ("NORMAL".equalsIgnoreCase(type)) {
            List<Column> tableColumns = new ArrayList<>(columns.size());
            for (UserIndexColumnImpl column : columns) {
                tableColumns.add(getColumn(table.getColumns(), column.getColumnName()));
            }
            Index.Builder builder = table.index(name).on(tableColumns.toArray(new Column[columns.size()]));
            if (compression > 0) {
                builder.compress(compression);
            }
            builder.add();
        }
    }

    private Column getColumn(List<? extends Column> columns, String name) {
        for (Column column : columns) {
            if (column.getName().equals(name)) {
                return column;
            }
        }
        return null;
    }
}
