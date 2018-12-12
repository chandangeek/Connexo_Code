/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;

abstract class QueryGroupConditionValue {
    enum Fields {
        GROUP_CONDITION("groupCondition"),
        VALUE("value"),
        POSITION("position");
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    @IsPresent
    private Reference<QueryGroupCondition> groupCondition = ValueReference.absent();
    @NotNull
    @Size(min = 1, max = Table.SHORT_DESCRIPTION_LENGTH)
    private String value;

    @SuppressWarnings("unused")
    private int position;
    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;
    @SuppressWarnings("unused") // Managed by ORM
    private String userName;

    QueryGroupConditionValue() {
    }

    QueryGroupConditionValue init(QueryGroupCondition groupCondition, String value) {
        this.groupCondition.set(groupCondition);
        this.value = value;
        return this;
    }

    public String getValue() {
        return value;
    }

}
