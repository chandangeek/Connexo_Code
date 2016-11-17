package com.elster.jupiter.usagepoint.lifecycle.impl;

import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointStateChangeFail;

import javax.validation.constraints.Size;

public class UsagePointStateChangeFailImpl implements UsagePointStateChangeFail {
    public enum Fields {
        CHANGE_REQUEST("changeRequest"),
        KEY("objectKey"),
        NAME("objectName"),
        MESSAGE("message");

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
    @Size(max = Table.NAME_LENGTH)
    private String objectKey;
    @Size(max = Table.NAME_LENGTH)
    private String objectName;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String message;

    public UsagePointStateChangeFailImpl init(UsagePointStateChangeRequestImpl request, String key, String name, String message) {
        this.changeRequest.set(request);
        this.objectKey = key;
        this.objectName = name;
        this.message = message;
        return this;
    }

    @Override
    public String getKey() {
        return this.objectKey;
    }

    @Override
    public String getName() {
        return this.objectName;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
