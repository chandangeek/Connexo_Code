package com.elster.jupiter.orm.schema.h2;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

public class ConstraintColumnImpl {

    private String columnName;
    @SuppressWarnings("unused")
    private int position;

    private Reference<ConstraintImpl> constraint = ValueReference.absent();

    ColumnImpl getColumn() {
        return constraint.get().getTable().getColumn(columnName);
    }

    String getColumnName() {
        return columnName;
    }

}
