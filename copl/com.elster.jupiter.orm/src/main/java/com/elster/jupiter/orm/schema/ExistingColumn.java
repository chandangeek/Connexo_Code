/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.schema;

public interface ExistingColumn {
    String getName();

    boolean isAutoId();

    String getTypeApi();

    boolean isNullable();

    boolean isConstraintPart();

    boolean isForeignKeyPart();

    boolean isVirtual();
}
