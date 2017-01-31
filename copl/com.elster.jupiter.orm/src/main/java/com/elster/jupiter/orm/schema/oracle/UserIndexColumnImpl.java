/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.schema.oracle;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

public class UserIndexColumnImpl {

    private String columnName;
    @SuppressWarnings("unused")
    private int position;

    private Reference<UserIndexImpl> index = ValueReference.absent();

    UserColumnImpl getColumn() {
        return index.get().getTable().getColumn(columnName);
    }

    String getColumnName() {
        return columnName;
    }

}
