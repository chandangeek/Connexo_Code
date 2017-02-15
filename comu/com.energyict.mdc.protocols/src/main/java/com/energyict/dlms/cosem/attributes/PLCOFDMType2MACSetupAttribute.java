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

public enum PLCOFDMType2MACSetupAttribute implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x0000),
    MAC_SHORT_ADDRESS(2, 0x0008),
    MAC_RC_COORD(3, 0x0010),
    MAC_PAN_ID(4, 0x0018),
    MAC_TONE_MASK(7, 0x0030),
    MAC_TMR_TTL(8, 0x0038),
    MAC_MAX_FRAME_RETRIES(9, 0x0040),
    MAC_NEIGHBOUR_TABLE_ENTRY_TTL(10, 0x0048),
    MAC_NEIGHBOUR_TABLE(11, 0x0050),
    MAC_HIGH_PRIORITY_WINDOW_SIZE(12, 0x0058),
    MAC_CSMA_FAIRNESS_LIMIT(13, 0x0060),
    MAC_BEACON_RANDOMIZATION_WINDOW_LENGTH(14, 0x0068),
    MAC_A(15, 0x0070),
    MAC_K(16, 0x0078),
    MAC_MIN_CW_ATTEMPTS(17, 0x0080),
    MAC_CENELEC_LEGACY_MODE(18, 0x0088),
    MAC_MAX_BE(20, 0x0098),
    MAC_MAX_CSMA_BACKOFF(21, 0x0100),
    MAC_MIN_BE(22, 0x0108);

    private final int attributeNumber;
    private final int shortName;

    private PLCOFDMType2MACSetupAttribute(int attrNr, int sn) {
        this.attributeNumber = attrNr;
        this.shortName = sn;
    }

    public int getAttributeNumber() {
        return attributeNumber;
    }

    public int getShortName() {
        return shortName;
    }

    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.PLC_OFDM_TYPE2_MAC_SETUP;
    }


    /**
     * @param attributeNumber
     * @return
     */
    public static PLCOFDMType2MACSetupAttribute findByAttributeNumber(int attributeNumber) {
        for (PLCOFDMType2MACSetupAttribute attribute : PLCOFDMType2MACSetupAttribute.values()) {
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
    public static PLCOFDMType2MACSetupAttribute findByShortName(int shortName) {
        for (PLCOFDMType2MACSetupAttribute attribute : PLCOFDMType2MACSetupAttribute.values()) {
            if (attribute.getShortName() == shortName) {
                return attribute;
            }
        }
        throw new IllegalArgumentException("No shortName found for id = " + shortName);
    }

}
