/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.attributes.PLCOFDMType2PHYAndMACCountersAttribute;
import com.energyict.dlms.cosem.methods.PLCOFDMType2PHYAndMACCountersMethods;

import java.io.IOException;

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
     * @throws IOException
     */
    public AbstractDataType readMacTxDataPacketCount() throws IOException {
        return readDataType(PLCOFDMType2PHYAndMACCountersAttribute.MAC_TX_DATA_PACKET_COUNT);
    }

    /**
     * Statistics counter of successfully received data packets (MSDU).
     *
     * @return
     * @throws IOException
     */
    public AbstractDataType readMacRxDataPacketCount() throws IOException {
        return readDataType(PLCOFDMType2PHYAndMACCountersAttribute.MAC_RX_DATA_PACKET_COUNT);
    }

    /**
     * Statistics counter of successfully transmitted command packets (MSDU).
     *
     * @return
     * @throws IOException
     */
    public AbstractDataType readMacTxCmdPacketCount() throws IOException {
        return readDataType(PLCOFDMType2PHYAndMACCountersAttribute.MAC_TX_CMD_PACKET_COUNT);
    }

    /**
     * Statistics counter of successfully received command packets (MSDU).
     *
     * @return
     * @throws IOException
     */
    public AbstractDataType readMacRxCmdPacketCount() throws IOException {
        return readDataType(PLCOFDMType2PHYAndMACCountersAttribute.MAC_RX_CMD_PACKET_COUNT);
    }

    /**
     * Statistic counter of failed CSMA transmit attempts.
     *
     * @return
     * @throws IOException
     */
    public AbstractDataType readMacCSMAFailCount() throws IOException {
        return readDataType(PLCOFDMType2PHYAndMACCountersAttribute.MAC_CSMA_FAIL_COUNT);
    }

    /**
     * Statistic counter of collisions due to busy channel or failed transmission on CSMA transmit attempts.
     *
     * @return
     * @throws IOException
     */
    public AbstractDataType readMacNoAckCount() throws IOException {
        return readDataType(PLCOFDMType2PHYAndMACCountersAttribute.MAC_NO_ACK_COUNT);
    }

    /**
     * Statistic counter of frames received with bad CRC.
     *
     * @return
     * @throws IOException
     */
    public AbstractDataType readMacBadCrcCount() throws IOException {
        return readDataType(PLCOFDMType2PHYAndMACCountersAttribute.MAC_BAD_CRC_COUNT);
    }

    /**
     * Statistic counter of broadcast frames sent
     *
     * @return
     * @throws IOException
     */
    public AbstractDataType readMacTxDataBroadcastCount() throws IOException {
        return readDataType(PLCOFDMType2PHYAndMACCountersAttribute.MAC_TX_DATA_BROADCAST_COUNT);
    }

    /**
     * Statistic counter of multicast frames sent
     *
     * @return
     * @throws IOException
     */
    public AbstractDataType readMacRxDataBroadcastCount() throws IOException {
        return readDataType(PLCOFDMType2PHYAndMACCountersAttribute.MAC_RX_DATA_BROADCAST_COUNT);
    }

    /**
     * This method forces a reset of the object. By invoking this method, the value of all
     * counters is set to 0.
     *
     * @throws IOException
     */
    public void reset() throws IOException {
        methodInvoke(PLCOFDMType2PHYAndMACCountersMethods.RESET, new Unsigned8(0));
    }

}
