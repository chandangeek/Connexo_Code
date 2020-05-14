/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.schema;

import com.elster.jupiter.orm.DataModel;

import aQute.bnd.annotation.ConsumerType;

import java.util.List;

@ConsumerType
public interface ExistingTable {
    String getName();

    List<? extends ExistingColumn> getColumns();

    List<? extends ExistingConstraint> getConstraints();

    List<? extends ExistingIndex> getIndexes();

    default void addTo(DataModel existingDataModel, String journalTableName) {
        addColumnsTo(existingDataModel, journalTableName);
        addIndexesTo(existingDataModel);
        addConstraintsTo(existingDataModel);
    }

    void addColumnsTo(DataModel existingDataModel, String journalTableName);

    default void addConstraintsTo(DataModel existingDataModel) {
        addLocalTableConstraintsTo(existingDataModel);
        addForeignKeyConstraintsTo(existingDataModel);
    }

    void addLocalTableConstraintsTo(DataModel existingDataModel);

    void addForeignKeyConstraintsTo(DataModel existingDataModel);

    void addIndexesTo(DataModel existingDataModel);
}
