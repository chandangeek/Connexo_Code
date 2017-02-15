/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem.attributes;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;

public enum ZigBeeSASJoinAttribute implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    SCAN_ATTEMPTS(2, 0x08),
    TIME_BETWEEN_SCANS(3, 0x10),
    REJOIN_INTERVAL(4, 0x18),
    MAX_REJOIN_INTERVAL(5, 0x20);

    /**
     * The number of this attribute
     */
    private final int attributeNumber;
    /**
     * The shortName of this attribute according to BlueBook V9
     */
    private final int shortName;

    ZigBeeSASJoinAttribute(final int attributeNumber, final int shortName) {
        this.attributeNumber = attributeNumber;
        this.shortName = shortName;
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
     * Getter for the DLMS class id
     *
     * @return The dlms class Id
     */
    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.ZIGBEE_SAS_JOIN;
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


}
