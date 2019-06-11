/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface AuditReference {

    String getName();

    Object getContextReference();

    boolean isRemoved();
}

