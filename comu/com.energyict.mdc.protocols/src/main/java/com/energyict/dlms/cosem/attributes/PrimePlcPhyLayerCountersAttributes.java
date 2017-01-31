/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem.attributes;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;

public enum PrimePlcPhyLayerCountersAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    CRC_INCORRECT_COUNT(2, 0x08),
    CRC_FAIL_COUNT(3, 0x10),
    TX_DROP_COUNT(4, 0x18),
    RX_DROP_COUNT(5, 0x20);

    private final int attributeNumber;
    private final int shortName;

    /**
     * Default constructor
     *
     * @param attributeNumber the number of the attribute (1-based)
     * @param shortName       the shortName of the attribute
     */
    private PrimePlcPhyLayerCountersAttributes(int attributeNumber, int shortName) {
        this.attributeNumber = attributeNumber;
        this.shortName = shortName;
    }

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.PRIME_PLC_PHY_LAYER_COUNTERS;
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
    public static PrimePlcPhyLayerCountersAttributes findByAttributeNumber(int attributeNumber) {
        for (PrimePlcPhyLayerCountersAttributes attribute : PrimePlcPhyLayerCountersAttributes.values()) {
            if (attribute.getAttributeNumber() == attributeNumber) {
                return attribute;
            }
        }
        throw new IllegalArgumentException("No attributeNumber found for id = " + attributeNumber);
    }

}
