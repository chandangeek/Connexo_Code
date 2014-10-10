package com.elster.jupiter.orm.schema;

import com.elster.jupiter.orm.DataModel;

import java.util.List;
import java.util.Optional;

public interface ExistingTable {
    String getName();

    List<? extends ExistingColumn> getColumns();

    List<? extends ExistingConstraint> getConstraints();

    List<? extends ExistingIndex> getIndexes();

    void addTo(DataModel existingDataModel, Optional<String> journalTableName);

}
