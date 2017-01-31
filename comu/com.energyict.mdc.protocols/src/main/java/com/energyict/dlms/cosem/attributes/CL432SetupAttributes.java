/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem.attributes;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;

public enum CL432SetupAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    DEVICE_ADDRESS(2, 0x08),
    BASE_NODE_ADDRESS(3, 0x10);

    private final int attributeNumber;
    private final int shortName;

    /**
     * Default constructor
     *
     * @param attributeNumber the number of the attribute (1-based)
     * @param shortName       the shortName of the attribute
     */
    private CL432SetupAttributes(int attributeNumber, int shortName) {
        this.attributeNumber = attributeNumber;
        this.shortName = shortName;
    }

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.CL_432_SETUP;
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
    public static CL432SetupAttributes findByAttributeNumber(int attributeNumber) {
        for (CL432SetupAttributes attribute : CL432SetupAttributes.values()) {
            if (attribute.getAttributeNumber() == attributeNumber) {
                return attribute;
            }
        }
        throw new IllegalArgumentException("No attributeNumber found for id = " + attributeNumber);
    }

}
