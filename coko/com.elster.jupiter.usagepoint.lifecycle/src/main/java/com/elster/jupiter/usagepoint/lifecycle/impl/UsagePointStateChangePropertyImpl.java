/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.impl;

import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

public class UsagePointStateChangePropertyImpl {
    public enum Fields {
        CHANGE_REQUEST("changeRequest"),
        KEY("objectKey"),
        VALUE("objectValue");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @IsPresent
    private Reference<UsagePointStateChangeRequestImpl> changeRequest = ValueReference.absent();
    private String objectKey;
    private String objectValue;

    public UsagePointStateChangePropertyImpl init(UsagePointStateChangeRequestImpl request, String key, String value) {
        this.objectKey = key;
        this.objectValue = value;
        this.changeRequest.set(request);
        return this;
    }

    public String getKey() {
        return this.objectKey;
    }

    public String getDatabaseValue() {
        return this.objectValue;
    }
}
