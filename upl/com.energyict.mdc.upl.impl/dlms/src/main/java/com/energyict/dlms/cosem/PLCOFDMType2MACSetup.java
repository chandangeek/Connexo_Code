package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.attributes.PLCOFDMType2MACSetupAttribute;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * An instance of this interface class holds the necessary parameters to set up the PLC OFDM Type 2 MAC IEEE 802.15.4 sub-layer.
 * These attributes influence the functional behaviour of an implementation. Implementations may allow changes to the attributes
 * during normal running, i.e. even after the device start-up sequence has been executed.
 * <p/>
 * Copyrights EnergyICT
 * Date: 20/03/12
 * Time: 7:57
 */
public class PLCOFDMType2MACSetup extends AbstractCosemObject {

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     * @param objectReference
     */
    public PLCOFDMType2MACSetup(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    public static ObisCode getDefaultObisCode() {
        return ObisCode.fromString("0.0.29.1.0.255");
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.PLC_OFDM_TYPE2_MAC_SETUP.getClassId();
    }

    /**
     * The 16-bit address that the device uses to communicate in the PAN.
     * <pre>
     * - 0x0000-0xFFFD Gives the short MAC address allocated by a coordinator
     *   during the registration of the device to the PAN coordinator.
     * - 0xFFFE Indicates that the device has found a PAN coordinator but
     *   a valid short MAC address has not yet been allocated
     * - 0xFFFF Indicates that the device has not found a PAN coordinator.
     * </pre>
     *
     * @return
     * @throws java.io.IOException
     */
    public int readShortAddress() throws IOException {
        return readDataType(PLCOFDMType2MACSetupAttribute.MAC_SHORT_ADDRESS).intValue();
    }

    /**
     * Indication of whether the device is associated to the PAN through the PAN coordinator.
     *
     * @return A value of TRUE indicates the device has associated through the PAN coordinator.
     * @throws java.io.IOException
     */
    public boolean readAssociatedPanCoord() throws IOException {
        return readDataType(PLCOFDMType2MACSetupAttribute.MAC_ASSOCIATED_PAN_COORD, BooleanObject.class).getState();
    }

    /**
     * The 16-bit short address assigned to the coordinator through which the device is associated
     * <pre>
     * - 0x0000–0xFFFD Gives the address of the PAN coordinator
     * - 0xFFFE Indicates that the coordinator is only using its 64-bit extended address
     * - 0xFFFF Indicates that the address of the PAN coordinator is unknown.
     * </pre>
     *
     * @return
     * @throws java.io.IOException
     */
    public int readCoordinatorShortAddress() throws IOException {
        return readDataType(PLCOFDMType2MACSetupAttribute.MAC_COORD_SHORT_ADDRESS).intValue();
    }

    /**
     * The 16-bit identifier of the PAN on which the device is operating. A value equal
     * to 0xFFFF indicates that the device is not associated
     *
     * @return
     * @throws java.io.IOException
     */
    public int readPanId() throws IOException {
        return readDataType(PLCOFDMType2MACSetupAttribute.MAC_PAN_ID).intValue();
    }

    /**
     * The number of hops necessary to reach the PAN coordinator (e.g. Data Concentrator).
     *
     * @return
     * @throws java.io.IOException
     */
    public int readNumberOfHops() throws IOException {
        return readDataType(PLCOFDMType2MACSetupAttribute.MAC_NUMBER_OF_HOPS).intValue();
    }

    /**
     * The maximum number of seconds without communication with a particular
     * device after which it is declared orphan (unit is second).
     *
     * @return
     * @throws java.io.IOException
     */
    public long readMaxOrphanTimer() throws IOException {
        return readDataType(PLCOFDMType2MACSetupAttribute.MAC_MAX_ORPHAN_TIMER).longValue();
    }

    /**
     * The neighbour table contains information about all the devices within the POS of
     * the device. One element of the table represents one PLC direct neighbour of the device.
     *
     * @return
     * @throws java.io.IOException
     */
    public Array readNeighborTable() throws IOException {
        return readDataType(PLCOFDMType2MACSetupAttribute.MAC_NEIGHBOR_TABLE, Array.class);
    }

    public BooleanObject readSecurityActivation() throws IOException {
        return readDataType(PLCOFDMType2MACSetupAttribute.MAC_SECURITY_ACTIVATION, BooleanObject.class);
    }

    /**
     * Write the maximum number of seconds without communication with a particular
     * device after which it is declared orphan (unit is second).
     *
     * @param maxOrphanTimer the timer value
     * @throws java.io.IOException
     */
    public void writeMaxOrphanTimer(long maxOrphanTimer) throws IOException {
        final Unsigned32 axdrMaxOrphanTimer = new Unsigned32(maxOrphanTimer);
        final byte[] rawData = axdrMaxOrphanTimer.getBEREncodedByteArray();
        write(PLCOFDMType2MACSetupAttribute.MAC_MAX_ORPHAN_TIMER, rawData);
    }

    /**
     * Write the 16-bit identifier of the PAN on which the device is operating. A value equal
     * to 0xFFFF indicates that the device is not associated
     *
     * @param panId the value of the pan id
     * @throws java.io.IOException
     */
    public void writePanID(int panId) throws IOException {
        final Unsigned16 axdrPanId = new Unsigned16(panId);
        final byte[] rawData = axdrPanId.getBEREncodedByteArray();
        write(PLCOFDMType2MACSetupAttribute.MAC_PAN_ID, rawData);
    }

}
