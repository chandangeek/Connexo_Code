package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.methods.ConcentratorSetupMethods;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Concentrator setup IC (for the Beacon 3100).
 *
 * Specified in : https://confluence.eict.vpdc/display/G3IntBeacon3100/DLMS+concentrator+specification.
 *
 * @author alex
 */
public final class ConcentratorSetup extends AbstractCosemObject {

    /** Beacon 3100 OBIS code. */
    public static final ObjectReference DEFAULT_OBIS_CODE = new ObjectReference(new byte[] { 0, 0, (byte)128, 0, 18, (byte)255 });

    /**
     * Create a new instance.
     *
     * @param 	protocolLink		The protocol link.
     * @param 	objectReference		The object reference, if any.
     */
    public ConcentratorSetup(final ProtocolLink protocolLink, final ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final int getClassId() {
        return DLMSClassId.CONCENTRATOR_SETUP.getClassId();
    }

    /**
     * Drops all persisted data in the concentrator: logical devices, meter reading data and setup (scheduler, device types, etc.)
     *
     * (Meters that are currently on the network will have to reassociate.)
     *
     * @throws 	IOException		If an IO error occurs.
     */
    public final void reset() throws IOException {
        this.methodInvoke(ConcentratorSetupMethods.RESET_CONCENTRATOR);
    }

    /**
     * Trigger the preliminary protocol for a particular meter (identified by it's MAC address (EUI64)).
     *
     * @param	mac					The MAC address of the meter to trigger the the protocol for.
     * @param	protocolName		The name of the protocol to use.
     */
    public final void triggerPreliminaryProtocol(final byte[] mac, final String protocolName) throws IOException {
        if (mac == null || mac.length != 8) {
            throw new IllegalArgumentException("MAC address should not be null and should be of length 8 !");
        }

        final Structure argument = new Structure();
        argument.addDataType(new OctetString(mac));
        argument.addDataType(new OctetString(protocolName.getBytes(StandardCharsets.US_ASCII)));

        this.methodInvoke(ConcentratorSetupMethods.TRIGGER_PRELIMINARY_PROTOCOL, argument);
    }
}
