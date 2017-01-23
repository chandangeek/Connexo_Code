package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.cosem.attributes.PrimePlcPhyLayerCountersAttributes;
import com.energyict.dlms.cosem.methods.PrimePlcPhyLayerCountersMethods;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * An instance of the "PRIME PLC physical layer counters" IC stores counters related to the physical
 * layers exchanges. The objective of these counters is to provide statistical information for
 * management purposes.
 * <p/>
 * Copyrights EnergyICT
 * Date: 6/6/12
 * Time: 9:05 AM
 */
public class PrimePlcPhyLayerCounters extends AbstractCosemObject {

    /**
     * The default {@link ObisCode} used in most case for this {@link PrimePlcPhyLayerCounters} cosem object
     */
    public static final ObisCode DEFAULT_OBIS_CODE = ObisCode.fromString("0.0.28.1.0.255");

    /**
     * Creates a new instance of {@link PrimePlcPhyLayerCounters}
     *
     * @param protocolLink    The protocol link to use for this object
     * @param objectReference The object reference to use, containing the obisCode
     */
    public PrimePlcPhyLayerCounters(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    /**
     * The default {@link ObisCode} used in most case for this {@link PrimePlcPhyLayerCounters} cosem object
     *
     * @return The default {@link ObisCode}
     */
    public static ObisCode getDefaultObisCode() {
        return DEFAULT_OBIS_CODE;
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.PRIME_PLC_PHY_LAYER_COUNTERS.getClassId();
    }

    /**
     * Holds the PIB variable 0xA0 specified in PRIME-R1.3E: Number of bursts received on the physical layer
     * for which the CRC was incorrect.
     *
     * @return the count value
     */
    public final Unsigned16 getCrcIncorrectCount() throws IOException {
        return readDataType(PrimePlcPhyLayerCountersAttributes.CRC_INCORRECT_COUNT, Unsigned16.class);
    }

    /**
     * Holds the PIB variable 0xA1 specified in PRIME-R1.3E:
     * Number of bursts received on the physical layer for which the CRC was correct, but
     * the Protocol field of PHY header had invalid value. This count would reflect number of
     * times corrupt data was received and the CRC calculation failed to detect it.
     *
     * @return the count value
     */
    public final Unsigned16 getCrcFailCount() throws IOException {
        return readDataType(PrimePlcPhyLayerCountersAttributes.CRC_FAIL_COUNT, Unsigned16.class);
    }

    /**
     * Holds the PIB variable 0xA2 specified in PRIME-R1.3E:
     * Number of times when the physical layer received new data to transmit (PHY_DATA
     * request) and had to either overwrite on existing data in its transmit queue or drop the
     * data in new request due to full queue.
     *
     * @return the count value
     */
    public final Unsigned16 getTxDropCount() throws IOException {
        return readDataType(PrimePlcPhyLayerCountersAttributes.TX_DROP_COUNT, Unsigned16.class);
    }

    /**
     * Holds the PIB variable 0xA3 specified in PRIME-R1.3E:
     * Number of times when the physical layer received new data on the channel and had
     * to either overwrite on existing data in its receive queue or drop the newly received
     * data due to full queue.
     *
     * @return the count value
     */
    public final Unsigned16 getRxDropCount() throws IOException {
        return readDataType(PrimePlcPhyLayerCountersAttributes.RX_DROP_COUNT, Unsigned16.class);
    }

    /**
     * This method resets the counter values.
     *
     * @throws java.io.IOException If the reset failed
     */
    public final void reset() throws IOException {
        methodInvoke(PrimePlcPhyLayerCountersMethods.RESET);
    }

}
