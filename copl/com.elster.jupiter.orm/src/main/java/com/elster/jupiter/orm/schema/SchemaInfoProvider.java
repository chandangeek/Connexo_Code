/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.schema;

import com.elster.jupiter.orm.DataModel;

import aQute.bnd.annotation.ConsumerType;

import java.util.List;

@ConsumerType
public interface SchemaInfoProvider {
    interface TableSpec {
        void addTo(DataModel model);
    }

    List<? extends TableSpec> getSchemaInfoTableSpec();

    default boolean isTestSchemaProvider() {
        return false;
    }
}
