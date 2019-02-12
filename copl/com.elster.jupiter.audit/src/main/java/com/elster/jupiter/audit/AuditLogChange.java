package com.elster.jupiter.audit;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface AuditLogChange {

    String getName();

    Object getValue();

    Object getPreviousValue();

    String getType();

    AuditLogChangeBuilder setName(String name);

    AuditLogChangeBuilder setValue(Object value);

    AuditLogChangeBuilder setPreviousValue(Object previousValue);

    AuditLogChangeBuilder setType(String type);

}

