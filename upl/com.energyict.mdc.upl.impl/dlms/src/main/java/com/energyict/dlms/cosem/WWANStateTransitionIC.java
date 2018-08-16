package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.attributes.WWANStateTransitionAttributes;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * class id = 1, version = 0, logical name = 0-162:96.192.0.255 (00A260C000FF)
 * This data IC provides access to the last state transition the modem performed.
 */
public class WWANStateTransitionIC extends AbstractCosemObject {

    public static final ObisCode OBIS_CODE = ObisCode.fromString("0.162.96.192.0.255");

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     * @param objectReference
     */
    public WWANStateTransitionIC(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    public static ObisCode getDefaultObisCode() {
        return OBIS_CODE;
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.DATA.getClassId();
    }

    /**
     * Last state transition. A state transition contains an additional reason which can be used for
     * troubleshooting.
     * state_transition ::= structure
     * {
     * timestamp: date-time -- date/time of state change
     * from_state: modem_state -- origin state
     * to_state: modem_state -- destination state
     * reason: octet-string -- reason for state transition
     * -- (empty if no information available)
     * }
     * modem_state ::= enum:
     * (0) stopped,
     * (1) unconfigured,
     * (2) configured,
     * (3) connecting,
     * (4) connected,
     * (5) disconnecting,
     * (255) unknown
     *
     * @return the state_transition DLMS {@link Structure}
     * @throws IOException if an exception occurs during the reading
     */
    public Structure readLastTransition() throws IOException {
        return readDataType(WWANStateTransitionAttributes.LAST_TRANSITION, Structure.class);
    }

}
