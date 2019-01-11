package com.energyict.mdc.device.data.impl.audit;

import com.elster.jupiter.audit.AuditLogChanges;

public class AuditLogChangesImpl implements AuditLogChanges {

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

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setPreviousValue(Object previousValue) {
        this.previousValue = previousValue;
    }

    public void setType(String type) {
        this.type = type;
    }
}
