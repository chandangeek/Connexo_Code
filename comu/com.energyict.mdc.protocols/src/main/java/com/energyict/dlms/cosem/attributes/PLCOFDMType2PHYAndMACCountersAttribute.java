/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 *
 */
package com.energyict.dlms.cosem.attributes;


import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;

public enum PLCOFDMType2PHYAndMACCountersAttribute implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x0000),
    MAC_TX_DATA_PACKET_COUNT(2, 0x0008),
    MAC_RX_DATA_PACKET_COUNT(3, 0x0010),
    MAC_TX_CMD_PACKET_COUNT(4, 0x0018),
    MAC_RX_CMD_PACKET_COUNT(5, 0x0020),
    MAC_CSMA_FAIL_COUNT(6, 0x0028),
    MAC_NO_ACK_COUNT(7, 0x0030),
    MAC_BAD_CRC_COUNT(8, 0x0038),
    MAC_TX_DATA_BROADCAST_COUNT(9, 0x0040),
    MAC_RX_DATA_BROADCAST_COUNT(10, 0x0048);

    private final int attributeNumber;
    private final int shortName;

    private PLCOFDMType2PHYAndMACCountersAttribute(int attrNr, int sn) {
        this.attributeNumber = attrNr;
        this.shortName = sn;
    }

    public int getAttributeNumber() {
        return attributeNumber;
    }

    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.PLC_OFDM_TYPE2_PHY_AND_MAC_COUNTERS;
    }

    public int getShortName() {
        return shortName;
    }

    /**
     * @param attributeNumber
     * @return
     */
    public static PLCOFDMType2PHYAndMACCountersAttribute findByAttributeNumber(int attributeNumber) {
        for (PLCOFDMType2PHYAndMACCountersAttribute attribute : PLCOFDMType2PHYAndMACCountersAttribute.values()) {
            if (attribute.getAttributeNumber() == attributeNumber) {
                return attribute;
            }
        }
        throw new IllegalArgumentException("No attributeNumber found for id = " + attributeNumber);
    }

    /**
     * @param shortName
     * @return
     */
    public static PLCOFDMType2PHYAndMACCountersAttribute findByShortName(int shortName) {
        for (PLCOFDMType2PHYAndMACCountersAttribute attribute : PLCOFDMType2PHYAndMACCountersAttribute.values()) {
            if (attribute.getShortName() == shortName) {
                return attribute;
            }
        }
        throw new IllegalArgumentException("No shortName found for id = " + shortName);
    }

}
