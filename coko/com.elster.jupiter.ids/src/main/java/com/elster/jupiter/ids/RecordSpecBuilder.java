/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.ids;

public interface RecordSpecBuilder {

    RecordSpecBuilder addFieldSpec(String name, FieldType type);

    RecordSpec create();
}
