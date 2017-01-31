/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem.attributes;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;

public enum PrimePlcMacFunctionalParametersAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    LNID(2, 0x08),
    LSID(3, 0x10),
    SID(4, 0x18),
    SNA(5, 0x20),
    STATE(6, 0x28),
    SCP_LENGTH(7, 0x30),
    NODE_HIERARCHY_LEVEL(8, 0x38),
    BEACON_SLOT_COUNT(9, 0x40),
    BEACON_RX_SLOT(10, 0x48),
    BEACON_TX_SLOT(11, 0x50),
    BEACON_RX_FREQUENCY(12, 0x58),
    BEACON_TX_FREQUENCY(13, 0x60);

    private final int attributeNumber;
    private final int shortName;

    /**
     * Default constructor
     *
     * @param attributeNumber the number of the attribute (1-based)
     * @param shortName       the shortName of the attribute
     */
    private PrimePlcMacFunctionalParametersAttributes(int attributeNumber, int shortName) {
        this.attributeNumber = attributeNumber;
        this.shortName = shortName;
    }

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.PRIME_PLC_MAC_FUNCTIONAL_PARAMETERS;
    }

    /**
     * Getter for the attribute number
     *
     * @return the attribute number as int
     */
    public int getAttributeNumber() {
        return this.attributeNumber;
    }

    /**
     * Getter for the short name
     *
     * @return the short name as int
     */
    public int getShortName() {
        return this.shortName;
    }

    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }

    /**
     * @param attributeNumber
     * @return
     */
    public static PrimePlcMacFunctionalParametersAttributes findByAttributeNumber(int attributeNumber) {
        for (PrimePlcMacFunctionalParametersAttributes attribute : PrimePlcMacFunctionalParametersAttributes.values()) {
            if (attribute.getAttributeNumber() == attributeNumber) {
                return attribute;
            }
        }
        throw new IllegalArgumentException("No attributeNumber found for id = " + attributeNumber);
    }

}
