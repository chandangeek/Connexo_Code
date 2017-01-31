/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.ids.impl;

import com.elster.jupiter.ids.FieldType;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.RecordSpecBuilder;
import com.elster.jupiter.orm.DataModel;

public class RecordSpecBuilderImpl implements RecordSpecBuilder {

    RecordSpecImpl underConstruction;

    public RecordSpecBuilderImpl(DataModel dataModel, String component, long id, String name) {
        underConstruction = dataModel.getInstance(RecordSpecImpl.class).init(component, id, name);
    }

    @Override
    public RecordSpecBuilderImpl addFieldSpec(String name, FieldType fieldType) {
        underConstruction.addFieldSpec(name, fieldType);
        return this;
    }

    @Override
    public RecordSpec create() {
        underConstruction.persist();
        return underConstruction;
    }
}
