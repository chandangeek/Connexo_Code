/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit;

import java.util.Objects;

public class AuditLogChangeBuilder implements AuditLogChange {

    private String name;
    private Object value;
    private Object previousValue;
    private String type;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public Object getPreviousValue() {
        return previousValue;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public AuditLogChangeBuilder setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public AuditLogChangeBuilder setValue(Object value) {
        this.value = value;
        return this;
    }

    @Override
    public AuditLogChangeBuilder setPreviousValue(Object previousValue) {
        this.previousValue = previousValue;
        return this;
    }

    @Override
    public AuditLogChangeBuilder setType(String type) {
        this.type = type;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AuditLogChange key = (AuditLogChange) o;
        return Objects.equals(name, key.getName()) &&
                Objects.equals(value, key.getValue()) &&
                Objects.equals(previousValue, key.getPreviousValue()) &&
                Objects.equals(type, key.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value, previousValue, type);
    }
}
