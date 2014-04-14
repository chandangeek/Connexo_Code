package com.elster.jupiter.schema.oracle;

import com.elster.jupiter.orm.DataModel;
import com.google.common.base.Optional;

import java.util.List;

public interface UserTable {
    String getName();

    List<? extends UserColumn> getColumns();

    List<String> generate();

    void addTo(DataModel existingDataModel, Optional<String> journalTableName);
}
