/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;

public class DedicatedEventLogSimple extends ProfileGeneric {

    /**
     * Creates a new instance of ProfileGeneric
     */
    public DedicatedEventLogSimple(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.DEDICATED_EVENT_LOG_SIMPLE.getClassId();
    }
}
