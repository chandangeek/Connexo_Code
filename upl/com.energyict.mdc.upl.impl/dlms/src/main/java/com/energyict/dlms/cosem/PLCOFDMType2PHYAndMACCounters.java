package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.attributes.PLCOFDMType2PHYAndMACCountersAttribute;
import com.energyict.dlms.cosem.methods.PLCOFDMType2PHYAndMACCountersMethods;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 19/03/12
 * Time: 16:59
 *
 * @author jme
 */
public class PLCOFDMType2PHYAndMACCounters extends AbstractCosemObject {

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     * @param objectReference
     */
    public PLCOFDMType2PHYAndMACCounters(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.PLC_OFDM_TYPE2_PHY_AND_MAC_COUNTERS.getClassId();
    }

    public static ObisCode getDefaultObisCode() {
        return ObisCode.fromString("0.0.29.0.0.255");
    }

    /**
     * Statistics counter of successfully transmitted data packets (MSDU).
     *
     * @return
     * @throws java.io.IOException
     */
    public long readMacTxDataPacketCount() throws IOException {
        return readDataType(PLCOFDMType2PHYAndMACCountersAttribute.MAC_TX_DATA_PACKET_COUNT).longValue();
    }

    /**
     * Statistics counter of successfully received data packets (MSDU).
     *
     * @return
     * @throws java.io.IOException
     */
    public long readMacRxDataPacketCount() throws IOException {
        return readDataType(PLCOFDMType2PHYAndMACCountersAttribute.MAC_RX_DATA_PACKET_COUNT).longValue();
    }

    /**
     * Statistics counter of successfully transmitted command packets (MSDU).
     *
     * @return
     * @throws java.io.IOException
     */
    public long readMacTxCmdPacketCount() throws IOException {
        return readDataType(PLCOFDMType2PHYAndMACCountersAttribute.MAC_TX_CMD_PACKET_COUNT).longValue();
    }

    /**
     * Statistics counter of successfully received command packets (MSDU).
     *
     * @return
     * @throws java.io.IOException
     */
    public long readMacRxCmdPacketCount() throws IOException {
        return readDataType(PLCOFDMType2PHYAndMACCountersAttribute.MAC_RX_CMD_PACKET_COUNT).longValue();
    }

    /**
     * Statistic counter of failed CSMA transmit attempts.
     *
     * @return
     * @throws java.io.IOException
     */
    public long readMacCSMAFailCount() throws IOException {
        return readDataType(PLCOFDMType2PHYAndMACCountersAttribute.MAC_CSMA_FAIL_COUNT).longValue();
    }

    /**
     * Statistic counter of collisions due to busy channel or failed transmission on CSMA transmit attempts.
     *
     * @return
     * @throws java.io.IOException
     */
    public long readMacCSMACollisionCount() throws IOException {
        return readDataType(PLCOFDMType2PHYAndMACCountersAttribute.MAC_CSMA_COLLISION_COUNT).longValue();
    }

    /**
     * Statistic counter of frames received with bad CRC.
     *
     * @return
     * @throws java.io.IOException
     */
    public long readMacBadCrcCount() throws IOException {
        return readDataType(PLCOFDMType2PHYAndMACCountersAttribute.MAC_BAD_CRC_COUNT).longValue();
    }

    /**
     * Statistic counter of broadcast frames sent
     *
     * @return
     * @throws java.io.IOException
     */
    public long readMacBroadcastCount() throws IOException {
        return readDataType(PLCOFDMType2PHYAndMACCountersAttribute.MAC_BROADCAST_COUNT).longValue();
    }

    /**
     * Statistic counter of multicast frames sent
     *
     * @return
     * @throws java.io.IOException
     */
    public long readMacMulticastCount() throws IOException {
        return readDataType(PLCOFDMType2PHYAndMACCountersAttribute.MAC_MULTICAST_COUNT).longValue();
    }

    /**
     * This method forces a reset of the object. By invoking this method, the value of all
     * counters is set to 0.
     *
     * @throws java.io.IOException
     */
    public void reset() throws IOException {
        methodInvoke(PLCOFDMType2PHYAndMACCountersMethods.RESET, new Unsigned8(0));
    }

}
