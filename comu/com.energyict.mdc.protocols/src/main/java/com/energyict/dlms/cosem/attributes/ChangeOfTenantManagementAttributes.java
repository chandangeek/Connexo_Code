/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem.attributes;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;

public enum ChangeOfTenantManagementAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    VALUE(2, 0x08),
    SCALER_UNIT(3, 0x10),
    PASSIVE_VALUE(4, 0x18),
    PASSIVE_SCALER_UNIT(5, 0x20),
    ACTIVATION_TIME(6, 0x28),;

    /**
     * The number of this attribute
     */
    private final int attributeNumber;
    /**
     * The shortName of this attribute
     */
    private final int shortName;

    ChangeOfTenantManagementAttributes(final int attributeNumber, final int shortName) {
        this.attributeNumber = attributeNumber;
        this.shortName = shortName;
    }

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.ACTIVE_PASSIVE;
    }

    /**
     * Getter for the short name
     *
     * @return the short name as int
     */
    public int getShortName() {
        return this.shortName;
    }

    /**
     * Getter for the attribute number
     *
     * @return the attribute number as int
     */
    public int getAttributeNumber() {
        return this.attributeNumber;
    }

    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }

}
