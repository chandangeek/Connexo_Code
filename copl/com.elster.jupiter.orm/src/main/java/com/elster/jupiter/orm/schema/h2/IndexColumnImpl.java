/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.schema.h2;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

public class IndexColumnImpl {

    private String indexName;
    private String columnName;
    @SuppressWarnings("unused")
    private int position;

    private Reference<TableImpl> table = ValueReference.absent();

    ColumnImpl getColumn() {
        return table.get().getColumn(columnName);
    }

    String getColumnName() {
        return columnName;
    }

    String getIndexName() {
        return indexName;
    }

    public int getPosition() {
        return position;
    }
}
