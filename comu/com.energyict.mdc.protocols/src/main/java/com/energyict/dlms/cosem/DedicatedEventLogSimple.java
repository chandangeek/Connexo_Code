package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;

/**
 * Custom object for the ZMD meter, extending the normal ProfileGeneric interface with some extra attributes.
 * Note that it has Class Id 10920 instead of 7
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 10/04/2015 - 13:05
 */
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
