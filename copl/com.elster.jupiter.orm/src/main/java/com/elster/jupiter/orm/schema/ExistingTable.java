/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.schema;

import com.elster.jupiter.orm.DataModel;

import java.util.List;

public interface ExistingTable {
    String getName();

    List<? extends ExistingColumn> getColumns();

    List<? extends ExistingConstraint> getConstraints();

    List<? extends ExistingIndex> getIndexes();

    default void addTo(DataModel existingDataModel, String journalTableName) {
        addColumnsTo(existingDataModel, journalTableName);
        addConstraintsTo(existingDataModel);
    }

    void addColumnsTo(DataModel existingDataModel, String journalTableName);

    void addConstraintsTo(DataModel existingDataModel);
}
