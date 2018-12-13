/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.schema;

import com.elster.jupiter.orm.DataModel;

import java.util.List;

public interface SchemaInfoProvider {
    public interface TableSpec {
        void addTo(DataModel model);
    }

    List<? extends TableSpec> getSchemaInfoTableSpec();
}
