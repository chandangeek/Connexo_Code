package com.elster.jupiter.audit;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface AuditLogChanges {

    String getName();

    Object getValue();

    Object getPreviousValue();

    String getType();

}

