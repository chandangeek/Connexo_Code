package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.attributes.SixLowPanAdaptationLayerSetupAttribute;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * An instance of this interface class holds the necessary parameters to set up the 6LoWPAN Adaptation layer.
 * <p/>
 * These attributes influence the functional behaviour of an implementation. Implementations may allow changes
 * to their values during normal running, i.e. even after the device start-up sequence has been executed.
 * <p/>
 * Copyrights EnergyICT
 * Date: 20/03/12
 * Time: 9:17
 */
public class SixLowPanAdaptationLayerSetup extends AbstractCosemObject {

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     * @param objectReference
     */
    public SixLowPanAdaptationLayerSetup(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    public static ObisCode getDefaultObisCode() {
        return ObisCode.fromString("0.0.29.2.0.255");
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.SIX_LOW_PAN_ADAPTATION_LAYER_SETUP.getClassId();
    }

    /**
     * Defines the maximum number of hops to be used by the routing
     * algorithm.
     *
     * @return
     * @throws java.io.IOException
     */
    public int readAdpMaxHops() throws IOException {
        return readDataType(SixLowPanAdaptationLayerSetupAttribute.ADP_MAX_HOPS).intValue();
    }

    /**
     * Defines the threshold below which a direct neighbour is not taken into
     * account during the commissioning procedure (based on LQI
     * measurement done by the physical layer).
     *
     * @return
     * @throws java.io.IOException
     */
    public int readAdpWeakLQIValue() throws IOException {
        return readDataType(SixLowPanAdaptationLayerSetupAttribute.ADP_WEAK_LQI_VALUE).intValue();
    }

    /**
     * Defines the number of seconds to wait between two consecutive
     * CONFLICT frames for the same conflicting PAN ID.
     *
     * @return
     * @throws java.io.IOException
     */
    public int readAdpPanConflictWait() throws IOException {
        return readDataType(SixLowPanAdaptationLayerSetupAttribute.ADP_PAN_CONFLICT_WAIT).intValue();
    }

    /**
     * Defines the maximum number of CONFLICT frames sent by a device for
     * the same PAN ID.
     *
     * @return
     * @throws java.io.IOException
     */
    public int readAdpMaxPanConflictCount() throws IOException {
        return readDataType(SixLowPanAdaptationLayerSetupAttribute.ADP_MAX_PAN_CONFLICT_COUNT).intValue();
    }

    /**
     * Defines the number of seconds of an active scan (association
     * procedure).
     *
     * @return
     * @throws java.io.IOException
     */
    public int readAdpActiveScanDuration() throws IOException {
        return readDataType(SixLowPanAdaptationLayerSetupAttribute.ADP_ACTIVE_SCAN_DURATION).intValue();
    }

    /**
     * Specifies the list of carriers (one bit = one carrier) to be used during
     * symbol formation. The use of this mask permits to interwork with other
     * PLC narrowband technology (e.g. IEC 61334-5-1 S-FSK) or to be
     * compliant with regulatory laws.
     *
     * @return
     * @throws java.io.IOException
     */
    public BitString readAdpToneMask() throws IOException {
        return readDataType(SixLowPanAdaptationLayerSetupAttribute.ADP_TONE_MASK, BitString.class);
    }

    /**
     * Defines the value of the maximum wait time in seconds between two
     * successive network discoveries.
     *
     * @return
     * @throws java.io.IOException
     */
    public int readAdpDiscoveryAttemptsSpeed() throws IOException {
        return readDataType(SixLowPanAdaptationLayerSetupAttribute.ADP_DISCOVERY_ATTEMPTS_SPEED).intValue();
    }

    /**
     * The routing configuration entity specifies all parameters linked to the
     * routing mechanism.
     *
     * @return
     * @throws java.io.IOException
     */
    public Array readAdpRoutingConfiguration() throws IOException {
        return readDataType(SixLowPanAdaptationLayerSetupAttribute.ADP_ROUTING_CONFIGURATION, Array.class);
    }

    /**
     * Defines the number of seconds below which an entry in the Broadcast
     * Table remains active in the table. On timer expiration, the entry is
     * automatically removed.
     *
     * @return
     * @throws java.io.IOException
     */
    public int readAdpBroadcastLogTableEntryTTL() throws IOException {
        return readDataType(SixLowPanAdaptationLayerSetupAttribute.ADP_BROADCAST_LOG_TABLE_ENTRY_TTL).intValue();
    }

    /**
     * Defines the number of seconds from which a new Tone Map Request
     * procedure must be done to update the neighbour characteristics in the
     * Neighbour Table.
     *
     * @return
     * @throws java.io.IOException
     */
    public int readAdpMaxAgeTime() throws IOException {
        return readDataType(SixLowPanAdaptationLayerSetupAttribute.ADP_MAXE_AGE_TIME).intValue();
    }

    /**
     * The routing table contains information about the different routes in
     * which the device is implicated.
     *
     * @return
     * @throws java.io.IOException
     */
    public Array readAdpRoutingTable() throws IOException {
        return readDataType(SixLowPanAdaptationLayerSetupAttribute.ADP_ROUTING_TABLE, Array.class);
    }

    /**
     * @param maxHops
     * @throws java.io.IOException
     */
    public void writeMaxHops(int maxHops) throws IOException {
        final Unsigned8 value = new Unsigned8(maxHops);
        final byte[] rawValue = value.getBEREncodedByteArray();
        write(SixLowPanAdaptationLayerSetupAttribute.ADP_MAX_HOPS, rawValue);
    }

    /**
     * @param weakLqiValue
     * @throws java.io.IOException
     */
    public void writeWeakLqiValue(int weakLqiValue) throws IOException {
        final Unsigned8 value = new Unsigned8(weakLqiValue);
        final byte[] rawValue = value.getBEREncodedByteArray();
        write(SixLowPanAdaptationLayerSetupAttribute.ADP_WEAK_LQI_VALUE, rawValue);
    }

    /**
     * @param panConflictWaitTime
     * @throws java.io.IOException
     */
    public void writePanConflictWaitTime(int panConflictWaitTime) throws IOException {
        final Unsigned16 value = new Unsigned16(panConflictWaitTime);
        final byte[] rawValue = value.getBEREncodedByteArray();
        write(SixLowPanAdaptationLayerSetupAttribute.ADP_PAN_CONFLICT_WAIT, rawValue);
    }

    /**
     * @param maxPanConflictCount
     * @throws java.io.IOException
     */
    public void writeMaxPanConflictCount(int maxPanConflictCount) throws IOException {
        final Unsigned8 value = new Unsigned8(maxPanConflictCount);
        final byte[] rawValue = value.getBEREncodedByteArray();
        write(SixLowPanAdaptationLayerSetupAttribute.ADP_MAX_PAN_CONFLICT_COUNT, rawValue);
    }

    /**
     * @param activeScanDuration
     * @throws java.io.IOException
     */
    public void writeActiveScanDuration(int activeScanDuration) throws IOException {
        final Unsigned8 value = new Unsigned8(activeScanDuration);
        final byte[] rawValue = value.getBEREncodedByteArray();
        write(SixLowPanAdaptationLayerSetupAttribute.ADP_ACTIVE_SCAN_DURATION, rawValue);
    }

    /**
     * @param toneMask
     * @throws java.io.IOException
     */
    public void writeToneMask(boolean[] toneMask) throws IOException {
        final BitString value = new BitString(toneMask);
        final byte[] rawValue = value.getBEREncodedByteArray();
        write(SixLowPanAdaptationLayerSetupAttribute.ADP_TONE_MASK, rawValue);
    }

    /**
     * @param discoveryAttemptsSpeed
     * @throws java.io.IOException
     */
    public void writeDiscoveryAttemptsSpeed(int discoveryAttemptsSpeed) throws IOException {
        final Unsigned16 value = new Unsigned16(discoveryAttemptsSpeed);
        final byte[] rawValue = value.getBEREncodedByteArray();
        write(SixLowPanAdaptationLayerSetupAttribute.ADP_DISCOVERY_ATTEMPTS_SPEED, rawValue);
    }

    /**
     * @param broadcastLogTableTTL
     * @throws java.io.IOException
     */
    public void writeBroadcastLogTableTTL(int broadcastLogTableTTL) throws IOException {
        final Unsigned16 value = new Unsigned16(broadcastLogTableTTL);
        final byte[] rawValue = value.getBEREncodedByteArray();
        write(SixLowPanAdaptationLayerSetupAttribute.ADP_BROADCAST_LOG_TABLE_ENTRY_TTL, rawValue);
    }

    public void writeMaxAgeTime(int maxAgeTime) throws IOException {
        final Unsigned16 value = new Unsigned16(maxAgeTime);
        final byte[] rawValue = value.getBEREncodedByteArray();
        write(SixLowPanAdaptationLayerSetupAttribute.ADP_MAXE_AGE_TIME, rawValue);
    }

}
