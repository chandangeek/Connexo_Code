/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.impl;

import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointStateChangeFail;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class UsagePointStateChangeFailImpl implements UsagePointStateChangeFail {

    public enum Fields {
        FAIL_SOURCE("failSource"),
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

    @NotNull
    private FailSource failSource;
    @IsPresent
    private Reference<UsagePointStateChangeRequestImpl> changeRequest = ValueReference.absent();
    @Size(max = Table.NAME_LENGTH)
    private String objectKey;
    @Size(max = Table.NAME_LENGTH)
    private String objectName;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String message;

    public UsagePointStateChangeFailImpl actionFail(UsagePointStateChangeRequestImpl request, String key, String name, String message) {
        this.failSource = FailSource.ACTION;
        return init(request, key, name, message);
    }

    public UsagePointStateChangeFailImpl checkFail(UsagePointStateChangeRequestImpl request, String key, String name, String message) {
        this.failSource = FailSource.CHECK;
        return init(request, key, name, message);
    }

    private UsagePointStateChangeFailImpl init(UsagePointStateChangeRequestImpl request, String key, String name, String message) {
        this.changeRequest.set(request);
        this.objectKey = key;
        this.objectName = name;
        this.message = message;
        return this;
    }

    public FailSource getFailSource() {
        return this.failSource;
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
