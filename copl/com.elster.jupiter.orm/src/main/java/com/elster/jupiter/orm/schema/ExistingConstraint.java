/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.schema;

import java.util.List;

public interface ExistingConstraint {
    String getName();

    boolean hasDefinition();

    String getType();

    boolean isForeignKey();

    List<ExistingColumn> getColumns();

    String getReferencedTableName();
}
